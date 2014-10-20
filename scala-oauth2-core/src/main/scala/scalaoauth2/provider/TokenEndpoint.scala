package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait TokenEndpoint {
  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    OAuthGrantType.AUTHORIZATION_CODE  -> new AuthorizationCode(),
    OAuthGrantType.REFRESH_TOKEN       -> new RefreshToken(),
    OAuthGrantType.CLIENT_CREDENTIALS  -> new ClientCredentials(),
    OAuthGrantType.PASSWORD            -> new Password()
  )

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[Either[OAuthError, GrantHandlerResult]] = try {
    val grantType = request.grantType.getOrElse(throw new InvalidRequest("grant_type is not found"))
    val handler = handlers.getOrElse(grantType, throw new UnsupportedGrantType("The grant_type is not supported"))

    fetcher.fetch(request).map { clientCredential =>
      dataHandler.validateClient(clientCredential, grantType).flatMap { validClient =>
        if (!validClient) {
          Future.successful(Left(throw new InvalidClient()))
        } else {
          handler.handleRequest(request, Some(clientCredential), dataHandler).map(Right(_))
        }
      }.recover {
        case e: OAuthError => Left(e)
      }
    }.getOrElse {
      if (handler.clientCredentialRequired) {
        throw new InvalidRequest("Client credential is not found")
      } else {
        handler.handleRequest(request, None, dataHandler).map(Right(_)).recover {
          case e: OAuthError => Left(e)
        }
      }
    }
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }
}

object TokenEndpoint extends TokenEndpoint
