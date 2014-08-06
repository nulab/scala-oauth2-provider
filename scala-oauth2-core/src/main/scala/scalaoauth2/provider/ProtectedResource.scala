package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ProtectedResource {

  val fetchers = Seq(AuthHeader, RequestParameter)

  def handleRequest[U](request: ProtectedResourceRequest, dataHandler: DataHandler[U]): Future[Either[OAuthError, AuthInfo[U]]] = try {
    fetchers.find { fetcher =>
      fetcher.matches(request)
    }.map { fetcher =>
      val result = fetcher.fetch(request)
      val accessToken: Future[Option[AccessToken]] = dataHandler.findAccessToken(result.token)

      accessToken.flatMap { optionalToken =>
        try {
          val token = optionalToken.getOrElse(throw new InvalidToken("Invalid access token"))
          if (dataHandler.isAccessTokenExpired(token)) {
            throw new ExpiredToken()
          }
          if (dataHandler.isAccessTokenExpired(token)) {
            throw new ExpiredToken()
          }

          val authInfo: Future[Option[AuthInfo[U]]] = dataHandler.findAuthInfoByAccessToken(token)

          authInfo.map(_.map(Right(_)).getOrElse(Left(new InvalidToken("invalid access token"))))
        } catch {
          case e: OAuthError => Future.successful(Left(e))
        }
      }
    }.getOrElse(throw new InvalidRequest("Access token was not specified"))
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }

}

object ProtectedResource extends ProtectedResource
