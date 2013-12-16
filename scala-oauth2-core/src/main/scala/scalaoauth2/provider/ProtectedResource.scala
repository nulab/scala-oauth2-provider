package scalaoauth2.provider

trait ProtectedResource {

  val fetchers = Seq(AuthHeader, RequestParameter)

  def handleRequest[U](request: ProtectedResourceRequest, dataHandler: DataHandler[U]): Either[OAuthError, AuthInfo[U]] = try {
    fetchers.find { fetcher =>
      fetcher.matches(request)
    }.map { fetcher =>
      val result = fetcher.fetch(request)
      val accessToken = dataHandler.findAccessToken(result.token).getOrElse(throw new InvalidToken("Invalid access token"))
      if (dataHandler.isAccessTokenExpired(accessToken)) {
        throw new ExpiredToken()
      }

      dataHandler.findAuthInfoByAccessToken(accessToken).map { Right(_) }.getOrElse(Left(new InvalidToken("invalid access token")))
    }.getOrElse(throw new InvalidRequest("Access token was not specified"))
  } catch {
    case e: OAuthError => Left(e)
  }

}

object ProtectedResource extends ProtectedResource
