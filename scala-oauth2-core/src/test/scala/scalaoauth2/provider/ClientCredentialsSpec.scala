package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ClientCredentialsSpec extends FlatSpec with ScalaFutures with OptionValues {

  it should "handle request" in {
    val clientCredentials = new ClientCredentials()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "client_secret" -> Seq("clientSecret1"), "scope" -> Seq("all")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = clientCredentials.handleRequest(clientCred, request, new MockDataHandler() {

      override def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = Future.successful(Some(MockUser(10000, "username")))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType should be("Bearer")
      result.accessToken should be("token1")
      result.expiresIn.value should (be <= 3600L and be > 3595L)
      result.refreshToken should be(None)
      result.scope should be(Some("all"))
    }
  }
}
