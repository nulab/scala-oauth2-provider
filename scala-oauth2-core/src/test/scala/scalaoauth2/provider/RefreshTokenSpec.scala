package scalaoauth2.provider

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

class RefreshTokenSpec extends FlatSpec with ScalaFutures {

  it should "handle request" in {
    val refreshToken = new RefreshToken(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("refresh_token" -> Seq("refreshToken1")))
    val f = refreshToken.handleRequest(request, new MockDataHandler() {

      override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[MockUser]]] =
        Future.successful(Some(AuthInfo(user = MockUser(10000, "username"), clientId = "clientId1", scope = None, redirectUri = None)))

      override def refreshAccessToken(authInfo: AuthInfo[MockUser], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("token1", Some(refreshToken), None, Some(3600), new java.util.Date()))

    })

    whenReady(f) { result =>
      result.tokenType should be ("Bearer")
      result.accessToken should be ("token1")
      result.expiresIn should be (Some(3600))
      result.refreshToken should be (Some("refreshToken1"))
      result.scope should be (None)
    }
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: AuthorizationRequest): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
