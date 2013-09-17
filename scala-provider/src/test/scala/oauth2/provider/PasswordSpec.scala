package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class PasswordSpec extends FlatSpec {

  it should "handle request" in {
    val password = new Password(new MockClientCredentialFetcher())
    val request = Request(Map(), Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all")))
    val grantHandlerResult = password.handleRequest(request, new MockDataHandler() {

      override def findUserId(username: String, password: String): Option[String] = Some("10000")

      override def createOrUpdateAuthInfo(userId: String, clientId: String, scope: Option[String]): Option[AuthInfo] = Some(
        AuthInfo(id = "1", userId = userId, clientId = clientId, refreshToken = Some("refreshToken1"), scope = scope, code = Some("code1"), redirectUri = Some("http://example.com/"))
      )

      override def createOrUpdateAccessToken(authInfo: AuthInfo): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())

    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (3600)
    grantHandlerResult.refreshToken should be (Some("refreshToken1"))
    grantHandlerResult.scope should be (Some("all"))
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: Request): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
