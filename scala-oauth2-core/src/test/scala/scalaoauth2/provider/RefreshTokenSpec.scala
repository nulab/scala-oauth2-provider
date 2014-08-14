package scalaoauth2.provider

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

class RefreshTokenSpec extends FlatSpec {

  it should "handle request" in {
    val refreshToken = new RefreshToken(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("refresh_token" -> Seq("refreshToken1")))
    val grantHandlerResultFuture = refreshToken.handleRequest(request, new MockDataHandler() {

      override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[MockUser]]] =
        Future.successful(Some(AuthInfo(user = MockUser(10000, "username"), clientId = "clientId1", scope = None, redirectUri = None)))

      override def refreshAccessToken(authInfo: AuthInfo[MockUser], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("token1", Some(refreshToken), None, Some(3600), new java.util.Date()))

    })

    val grantHandlerResult = Await.result(grantHandlerResultFuture, 5.seconds)

    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (Some(3600))
    grantHandlerResult.refreshToken should be (Some("refreshToken1"))
    grantHandlerResult.scope should be (None)
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: AuthorizationRequest): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
