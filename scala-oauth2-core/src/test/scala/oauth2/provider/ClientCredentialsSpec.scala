package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class ClientCredentialsSpec extends FlatSpec {

  it should "handle request" in {
    val clientCredentials = new ClientCredentials(new MockClientCredentialFetcher())
    val request = Request(Map(), Map("scope" -> Seq("all")))
    val grantHandlerResult = clientCredentials.handleRequest(request, new MockDataHandler() {

      override def findClientUser(clientId: String, clientSecret: String): Option[MockUser] = Some(MockUser(10000, "username"))

      override def createOrUpdateAuthInfo(user: MockUser, clientId: String, scope: Option[String]): Option[AuthInfo[MockUser]] = Some(
        AuthInfo(id = "1", user = user, clientId = clientId, refreshToken = None, scope = scope, code = Some("code1"), redirectUri = Some("http://example.com/"))
      )

      override def createOrUpdateAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())
    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (3600)
    grantHandlerResult.refreshToken should be (None)
    grantHandlerResult.scope should be (Some("all"))
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: Request): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
