package scalaoauth2.provider

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers._

class RefreshTokenSpec extends FlatSpec {

  it should "handle request" in {
    val refreshToken = new RefreshToken(new MockClientCredentialFetcher())
    val request = Request(Map(), Map("refresh_token" -> Seq("refreshToken1")))
    val grantHandlerResult = refreshToken.handleRequest(request, new MockDataHandler() {

      override def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo] =
        Some(AuthInfo(id = "1", userId = "10000", clientId = "clientId1", refreshToken = Some("refreshToken1"), scope = None, code = None, redirectUri = None))

      override def createOrUpdateAccessToken(authInfo: AuthInfo): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())

    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (3600)
    grantHandlerResult.refreshToken should be (Some("refreshToken1"))
    grantHandlerResult.scope should be (None)
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: Request): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
