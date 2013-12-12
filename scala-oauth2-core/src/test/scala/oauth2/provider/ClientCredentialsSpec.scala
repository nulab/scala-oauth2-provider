package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class ClientCredentialsSpec extends FlatSpec {

  it should "handle request" in {
    val clientCredentials = new ClientCredentials(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("scope" -> Seq("all")))
    val grantHandlerResult = clientCredentials.handleRequest(request, new MockDataHandler() {

      override def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Option[MockUser] = Some(MockUser(10000, "username"))

      override def createAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date())
    })
    grantHandlerResult.tokenType should be ("Bearer")
    grantHandlerResult.accessToken should be ("token1")
    grantHandlerResult.expiresIn should be (Some(3600))
    grantHandlerResult.refreshToken should be (None)
    grantHandlerResult.scope should be (Some("all"))
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: AuthorizationRequest): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
