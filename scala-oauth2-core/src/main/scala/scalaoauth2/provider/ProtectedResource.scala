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
      dataHandler.findAccessToken(result.token).flatMap { optionalToken =>
        val token = optionalToken.getOrElse(throw new InvalidToken("The access token is not found"))
        if (dataHandler.isAccessTokenExpired(token)) {
          throw new ExpiredToken()
        }

        dataHandler.findAuthInfoByAccessToken(token).map(_.map(Right(_)).getOrElse(Left(new InvalidToken("The access token is invalid"))))
      }.recover {
        case e: OAuthError => Left(e)
      }
    }.getOrElse(throw new InvalidRequest("Access token is not found"))
  } catch {
    case e: OAuthError => Future.successful(Left(e))
  }

}

object ProtectedResource extends ProtectedResource
