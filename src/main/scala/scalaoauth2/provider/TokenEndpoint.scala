package scalaoauth2.provider

import scala.concurrent.{ExecutionContext, Future}

trait TokenEndpoint {
  val handlers = Map.empty[String, GrantHandler]

  def handleRequest[U](
      request: AuthorizationRequest,
      handler: AuthorizationHandler[U]
  )(
      implicit ctx: ExecutionContext
  ): Future[Either[OAuthError, GrantHandlerResult[U]]] =
    try {
      val grantType = request.grantType
      val grantHandler = () =>
        handlers.getOrElse(
          grantType,
          throw new UnsupportedGrantType(s"$grantType is not supported")
        )

      request.parseClientCredential
        .map { maybeCredential =>
          maybeCredential.fold(
            invalid => Future.successful(Left(invalid)),
            clientCredential => {
              handler
                .validateClient(Some(clientCredential), request)
                .flatMap { isValidClient =>
                  if (!isValidClient) {
                    Future.successful(
                      Left(
                        new InvalidClient(
                          "Invalid client or client is not authorized"
                        )
                      )
                    )
                  } else {
                    grantHandler()
                      .handleRequest(Some(clientCredential), request, handler)
                      .map(Right(_))
                  }
                }
                .recover {
                  case e: OAuthError => Left(e)
                }
            }
          )
        }
        .getOrElse {
          val gh = grantHandler()
          if (gh.clientCredentialRequired) {
            throw new InvalidRequest("Client credential is not found")
          } else {
            gh.handleRequest(None, request, handler).map(Right(_)).recover {
              case e: OAuthError => Left(e)
            }
          }
        }
    } catch {
      case e: OAuthError => Future.successful(Left(e))
    }
}

object TokenEndpoint extends TokenEndpoint
