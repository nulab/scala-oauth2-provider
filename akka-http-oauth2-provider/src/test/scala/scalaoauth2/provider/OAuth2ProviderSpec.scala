package scalaoauth2.provider

import java.util.Date

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.FormData
import scala.concurrent.Future

class OAuth2ProviderSpec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val tokenEndpointCredentials = new TokenEndpoint {
    override val handlers = Map(
      OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials
    )
  }

  val oauth2ProviderFail = new OAuth2Provider[User] {
    override val oauth2DataHandler = new MockDataHandler()
    override val tokenEndpoint = tokenEndpointCredentials
  }

  val user = MockUser(1, "user")
  val someAuthInfo = Some(AuthInfo(user, Some("clientId"), None, None))
  val accessToken = AccessToken("token", Some("refresh token"), None, Some(3600), new Date)

  val oauth2ProviderSuccess = new OAuth2Provider[User] {
    override val tokenEndpoint = tokenEndpointCredentials
    override val oauth2DataHandler = new MockDataHandler() {
      override def findAccessToken(token: String): Future[Option[AccessToken]] =
        Future.successful(Some(accessToken))
      override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] =
        Future.successful(someAuthInfo)
      override def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] =
        Future.successful(Some(user))
      override def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] =
        Future.successful(true)
      override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] =
        Future.successful(Some(accessToken))
      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] =
        Future.successful(accessToken)
    }
  }

  "oauth2Authenticator" should {

    "return none when data handler cannot find access token" in {
      val r = oauth2ProviderFail.oauth2Authenticator(Credentials(Some(OAuth2BearerToken("token"))))
      whenReady(r) { result => result should be(None) }
    }

    "return none when there is not a bearer token in request" in {
      val r = oauth2ProviderSuccess.oauth2Authenticator(Credentials(None))
      whenReady(r) { result => result should be(None) }
    }

    "return some authinfo when there is a token match" in {
      val r = oauth2ProviderSuccess.oauth2Authenticator(Credentials(Some(OAuth2BearerToken("token"))))
      whenReady(r) { result => result should be(someAuthInfo) }
    }

  }

  "access token route" should {

    "return Unauthorized when there is an error on authorization" in {
      Post("/oauth/access_token", FormData(
        "client_id" -> "bob_client_id",
        "client_secret" -> "bob_client_secret", "grant_type" -> "client_credentials"
      )) ~> oauth2ProviderFail.accessTokenRoute ~> check {
        handled shouldEqual true
        status shouldEqual Unauthorized
      }
    }

    "return Ok with token respons when there is a valid authorization" in {
      Post("/oauth/access_token", FormData(
        "client_id" -> "bob_client_id",
        "client_secret" -> "bob_client_secret", "grant_type" -> "client_credentials"
      )) ~> oauth2ProviderSuccess.accessTokenRoute ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

  }

}