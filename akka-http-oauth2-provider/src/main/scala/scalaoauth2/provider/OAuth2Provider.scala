package scalaoauth2.provider

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.Credentials
import scalaoauth2.provider.OAuth2Provider.TokenResponse
import spray.json.{ JsValue, DefaultJsonProtocol }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait OAuth2Provider[U] extends Directives with DefaultJsonProtocol {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val oauth2DataHandler: DataHandler[U]

  val tokenEndpoint: TokenEndpoint

  def grantResultToTokenResponse(grantResult: GrantHandlerResult[U]): JsValue =
    OAuth2Provider.tokenResponseFormat.write(TokenResponse(grantResult.tokenType, grantResult.accessToken, grantResult.expiresIn.getOrElse(1L), grantResult.refreshToken.getOrElse("")))

  def oauth2Authenticator(credentials: Credentials): Future[Option[AuthInfo[U]]] =
    credentials match {
      case Credentials.Provided(token) =>
        oauth2DataHandler.findAccessToken(token).flatMap {
          case Some(token) => oauth2DataHandler.findAuthInfoByAccessToken(token)
          case None => Future.successful(None)
        }
      case _ => Future.successful(None)
    }

  def accessTokenRoute = pathPrefix("oauth") {
    path("access_token") {
      post {
        formFieldMap { fields =>
          onComplete(tokenEndpoint.handleRequest(new AuthorizationRequest(Map(), fields.map(m => m._1 -> Seq(m._2))), oauth2DataHandler)) {
            case Success(maybeGrantResponse) =>
              maybeGrantResponse.fold(
                oauthError => complete(Unauthorized),
                grantResult => complete(grantResultToTokenResponse(grantResult))
              )
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }

}

object OAuth2Provider extends DefaultJsonProtocol {
  case class TokenResponse(token_type: String, access_token: String, expires_in: Long, refresh_token: String)
  implicit val tokenResponseFormat = jsonFormat4(TokenResponse)
}
