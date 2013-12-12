package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class AuthorizationCodeSpec extends FlatSpec {

  it should "handle request" in {
    val authorizationCode = new AuthorizationCode(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val grantHandlerResult = authorizationCode.handleRequest(request, new MockDataHandler() {
      
      override def findAuthInfoByCode(code: String): Option[AuthInfo[MockUser]] = Some(
        AuthInfo(id = "1", user = MockUser(10000, "username"), clientId = "clientId1", refreshToken = Some("refreshToken1"), scope = Some("all"), code = Some("code1"), redirectUri = Some("http://example.com/"))
      )

      override def createOrUpdateAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())
    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (3600)
    grantHandlerResult.refreshToken should be (Some("refreshToken1"))
    grantHandlerResult.scope should be (Some("all"))
  }

  it should "handle request if redirectUrl is none" in {
    val authorizationCode = new AuthorizationCode(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val grantHandlerResult = authorizationCode.handleRequest(request, new MockDataHandler() {
      
      override def findAuthInfoByCode(code: String): Option[AuthInfo[MockUser]] = Some(
        AuthInfo(id = "1", user = MockUser(10000, "username"), clientId = "clientId1", refreshToken = Some("refreshToken1"), scope = Some("all"), code = Some("code1"), redirectUri = None)
      )

      override def createOrUpdateAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())
    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (3600)
    grantHandlerResult.refreshToken should be (Some("refreshToken1"))
    grantHandlerResult.scope should be (Some("all"))
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: AuthorizationRequest): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
