package scalaoauth2.provider

import java.util.Date

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Future

class OAuth2ProviderSpec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val oauth2ProviderFail = new OAuth2Provider[User] {
    override val oauth2DataHandler = new MockDataHandler()
  }

  val someAuthInfo = Some(AuthInfo(MockUser(1, "user"), Some("clientId"), None, None))

  val oauth2ProviderSuccess = new OAuth2Provider[User] {
    override val oauth2DataHandler = new MockDataHandler() {
      override def findAccessToken(token: String): Future[Option[AccessToken]] =
        Future.successful(Some(AccessToken("token", Some("refresh token"), None, Some(3600), new Date)))
      override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] =
        Future.successful(someAuthInfo)
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

}