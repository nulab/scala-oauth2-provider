package scalaoauth2.provider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TokenEndpoint {
  val handlers = Map.empty[String, GrantHandler]

  def handleRequest[U](request: AuthorizationRequest, handler: AuthorizationHandler[U]): Future[Either[OAuthError, GrantHandlerResult[U]]] = try {
    val grantType = request.grantType
    val grantHandler = handlers.getOrElse(grantType, throw new UnsupportedGrantType(s"${grantType} is not supported"))

    request.clientCredential.map { clientCredential =>
      handler.validateClient(request).flatMap { validClient =>
        if (!validClient) {
          Future.successful(Left(new InvalidClient("Invalid client is detected")))
        } else {
          grantHandler.handleRequest(request, handler).map(Right(_))
        }
      }.recover {
        case e: OAuthError => Left(e)
      }
    }.getOrElse {
      if (grantHandler.clientCredentialRequired) {
        throw new InvalidRequest("Client credential is not found")
      } else {
        grantHandler.handleRequest(request, handler).map(Right(_)).recover {
          case e: OAuthError => Left(e)
        }
      }
    }
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }
}

object TokenEndpoint extends TokenEndpoint
