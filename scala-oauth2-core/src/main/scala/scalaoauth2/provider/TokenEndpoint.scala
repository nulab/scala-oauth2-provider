package scalaoauth2.provider

class TokenEndpoint {

  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    "authorization_code" -> new AuthorizationCode(fetcher),
    "refresh_token" -> new RefreshToken(fetcher),
    "client_credentials" -> new ClientCredentials(fetcher),
    "password" -> new Password(fetcher)
  )

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Either[OAuthError, GrantHandlerResult] = try {
    val grantType = request.grantType.getOrElse(throw new InvalidRequest("grant_type not found"))
    val handler = handlers.get(grantType).getOrElse(throw new UnsupportedGrantType("the grant_type isn't supported"))
    val clientCredential = fetcher.fetch(request).getOrElse(throw new InvalidRequest("client credential not found"))
    if (!dataHandler.validateClient(clientCredential.clientId, clientCredential.clientSecret, grantType)) {
      throw new InvalidClient
    }

    Right(handler.handleRequest(request, dataHandler))
  } catch {
    case e: OAuthError => Left(e)
  }
}

object TokenEndpoint extends TokenEndpoint