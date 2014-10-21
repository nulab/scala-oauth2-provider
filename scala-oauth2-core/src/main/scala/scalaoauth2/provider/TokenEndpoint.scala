package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaoauth2.provider.OAuthGrantType._

trait TokenEndpoint {
  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    AUTHORIZATION_CODE  -> new AuthorizationCode(),
    REFRESH_TOKEN       -> new RefreshToken(),
    CLIENT_CREDENTIALS  -> new ClientCredentials(),
    PASSWORD            -> new Password()
  )

  def handleRequest[U](request: AuthorizationRequest, handler: AuthorizationHandler[U]): Future[Either[OAuthError, GrantHandlerResult]] = try {
    val grantType = request.grantType.getOrElse(throw new InvalidRequest("grant_type is not found"))
    val grantHandler = handlers.getOrElse(grantType, throw new UnsupportedGrantType("The grant_type is not supported"))

    fetcher.fetch(request).map { clientCredential =>
      handler.validateClient(clientCredential, grantType).flatMap { validClient =>
        if (!validClient) {
          Future.successful(Left(throw new InvalidClient()))
        } else {
          grantHandler.handleRequest(request, Some(clientCredential), handler).map(Right(_))
        }
      }.recover {
        case e: OAuthError => Left(e)
      }
    }.getOrElse {
      if (grantHandler.clientCredentialRequired) {
        throw new InvalidRequest("Client credential is not found")
      } else {
        grantHandler.handleRequest(request, None, handler).map(Right(_)).recover {
          case e: OAuthError => Left(e)
        }
      }
    }
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }
}

object TokenEndpoint extends TokenEndpoint
