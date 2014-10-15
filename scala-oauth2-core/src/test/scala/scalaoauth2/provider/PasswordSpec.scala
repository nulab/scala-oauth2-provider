package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class PasswordSpec extends FlatSpec with ScalaFutures {

  val passwordClientCredReq = new Password()
  val passwordNoClientCredReq = new Password() {
    override def clientCredentialRequired = false
  }

  "Password when client credential required" should "handle request" in handlesRequest(passwordClientCredReq, Some(ClientCredential("clientId1", Some("clientSecret1"))))
  "Password when client credential not required" should "handle request" in handlesRequest(passwordNoClientCredReq, None)

  def handlesRequest(password: Password, clientCredential: Option[ClientCredential]) = {
    val request = AuthorizationRequest(Map(), Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all")))
    val f = password.handleRequest(request, clientCredential, new MockDataHandler() {

      override def findUser(username: String, password: String): Future[Option[User]] = Future.successful(Some(MockUser(10000, "username")))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))

    })

    whenReady(f) { result =>
      result.tokenType should be("Bearer")
      result.accessToken should be("token1")
      result.expiresIn should be(Some(3600))
      result.refreshToken should be(Some("refreshToken1"))
      result.scope should be(Some("all"))
    }
  }
}
