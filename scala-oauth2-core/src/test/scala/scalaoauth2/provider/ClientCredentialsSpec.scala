package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class ClientCredentialsSpec extends FlatSpec with ScalaFutures {

  it should "handle request" in {
    val clientCredentials = new ClientCredentials()
    val request = AuthorizationRequest(Map(), Map("scope" -> Seq("all")))
    val f = clientCredentials.handleRequest(request, Some(ClientCredential("clientId1", Some("clientSecret1"))), new MockDataHandler() {

      override def findClientUser(clientCredential: ClientCredential, scope: Option[String]): Future[Option[User]] = Future.successful(Some(MockUser(10000, "username")))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType should be ("Bearer")
      result.accessToken should be ("token1")
      result.expiresIn should be (Some(3600))
      result.refreshToken should be (None)
      result.scope should be (Some("all"))
    }
  }
}
