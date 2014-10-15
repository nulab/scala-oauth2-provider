package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait TokenEndpoint {
  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    "authorization_code" -> new AuthorizationCode(),
    "refresh_token" -> new RefreshToken(),
    "client_credentials" -> new ClientCredentials(),
    "password" -> new Password()
  )

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[Either[OAuthError, GrantHandlerResult]] = try {
    val grantType = request.grantType.getOrElse(throw new InvalidRequest("grant_type is not found"))
    val handler = handlers.get(grantType).getOrElse(throw new UnsupportedGrantType("The grant_type is not supported"))

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
