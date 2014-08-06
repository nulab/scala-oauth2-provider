package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TokenEndpoint {

  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    "authorization_code" -> new AuthorizationCode(fetcher),
    "refresh_token" -> new RefreshToken(fetcher),
    "client_credentials" -> new ClientCredentials(fetcher),
    "password" -> new Password(fetcher)
  )

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[Either[OAuthError, GrantHandlerResult]] = try {
    val grantType = request.grantType.getOrElse(throw new InvalidRequest("grant_type not found"))
    val handler = handlers.get(grantType).getOrElse(throw new UnsupportedGrantType("the grant_type isn't supported"))
    val clientCredential = fetcher.fetch(request).getOrElse(throw new InvalidRequest("client credential not found"))

    val validateClientFuture = dataHandler.validateClient(clientCredential.clientId, clientCredential.clientSecret, grantType)

    validateClientFuture.flatMap { validClient =>
      try {
        if (!validClient) throw new InvalidClient
        handler.handleRequest(request, dataHandler).map(Right(_))
      } catch {
        case e: OAuthError => Future.successful(Left(e))
      }
    }
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }
}

object TokenEndpoint extends TokenEndpoint
