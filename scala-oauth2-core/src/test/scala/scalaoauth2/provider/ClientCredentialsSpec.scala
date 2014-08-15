package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

class ClientCredentialsSpec extends FlatSpec with ScalaFutures {

  it should "handle request" in {
    val clientCredentials = new ClientCredentials(new MockClientCredentialFetcher())
    val request = AuthorizationRequest(Map(), Map("scope" -> Seq("all")))
    val f = clientCredentials.handleRequest(request, new MockDataHandler() {

      override def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Future[Option[MockUser]] = Future.successful(Some(MockUser(10000, "username")))

      override def createAccessToken(authInfo: AuthInfo[MockUser]): Future[AccessToken] = Future.successful(AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType should be ("Bearer")
      result.accessToken should be ("token1")
      result.expiresIn should be (Some(3600))
      result.refreshToken should be (None)
      result.scope should be (Some("all"))
    }
  }

  class MockClientCredentialFetcher extends ClientCredentialFetcher {

    override def fetch(request: AuthorizationRequest): Option[ClientCredential] = Some(ClientCredential("clientId1", "clientSecret1"))

  }
}
