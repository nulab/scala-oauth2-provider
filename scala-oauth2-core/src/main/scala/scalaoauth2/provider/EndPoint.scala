package scalaoauth2.provider

case class Request(headers: Map[String, String], params: Map[String, Seq[String]]) {

  def header(name: String): Option[String] = headers.get(name)

  def requireHeader(name: String): String = headers.get(name).getOrElse(throw new InvalidRequest("required header: " + name))

  def param(name: String): Option[String] = params.get(name).flatMap { values => values.headOption }

  def requireParam(name: String): String = param(name).getOrElse(throw new InvalidRequest("required parameter: " + name))

}

trait ProtectedResource {

  val fetchers = Seq(AuthHeader, RequestParameter)

  def handleRequest(request: Request, dataHandler: DataHandler): Either[OAuthError, AuthInfo] = try {
    fetchers.find { fetcher =>
      fetcher.matches(request)
    }.map { fetcher =>
      val result = fetcher.fetch(request)
      val accessToken = dataHandler.findAccessToken(result.token).getOrElse(throw new InvalidToken("Invalid access token"))
      val now = System.currentTimeMillis()
      if (accessToken.createdAt.getTime + accessToken.expiresIn * 1000 <= now) {
        throw new ExpiredToken()
      }

      dataHandler.findAuthInfoById(accessToken.authId).map { Right(_) }.getOrElse(Left(new InvalidToken("invalid access token")))
    }.getOrElse(throw new InvalidRequest("Access token was not specified"))
  } catch {
    case e: OAuthError => Left(e)
  }

}

object ProtectedResource extends ProtectedResource

trait Token {

  val fetcher = ClientCredentialFetcher

  val handlers = Map(
    "authorization_code" -> new AuthorizationCode(fetcher),
    "refresh_token" -> new RefreshToken(fetcher),
    "client_credentials" -> new ClientCredentials(fetcher),
    "password" -> new Password(fetcher)
  )

  def handleRequest(request: Request, dataHandler: DataHandler): Either[OAuthError, GrantHandlerResult] = try {
    val grantType = request.param("grant_type").getOrElse(throw new InvalidRequest("grant_type not found"))
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

object Token extends Token
