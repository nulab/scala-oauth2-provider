package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class ImplicitSpec extends FlatSpec with ScalaFutures with OptionValues {

  val implicitGrant = new Implicit()

  "Implicit" should "grant access with valid user authentication" in handlesRequest(implicitGrant, "user", "pass", true)
  "Implicit" should "not grant access with invalid user authentication" in handlesRequest(implicitGrant, "user", "wrong_pass", false)

  def handlesRequest(implicitGrant: Implicit, user: String, pass: String, ok: Boolean) = {
    val request = AuthorizationRequest(Map(), Map("client_id" -> Seq("client"), "username" -> Seq(user), "password" -> Seq(pass), "scope" -> Seq("all")))
    val f = implicitGrant.handleRequest(request, None, new MockDataHandler() {

      override def findUser(request: AuthorizationRequest): Future[Option[User]] =
        Future.successful(if(request.requireUsername == "user" && request.requirePassword == "pass") Some(MockUser(10000, "username")) else None)

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] =
        Future.successful(AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date()))

    })

    if(ok) {
      whenReady(f) { result =>
        result.tokenType should be("Bearer")
        result.accessToken should be("token1")
        result.expiresIn.value should (be <= 3600L and be > 3595L)
        result.refreshToken should be(None)
        result.scope should be(Some("all"))
      }
    } else {
      whenReady(f.failed) { e =>
        e shouldBe an[InvalidGrant]
      }
    }
  }
}
