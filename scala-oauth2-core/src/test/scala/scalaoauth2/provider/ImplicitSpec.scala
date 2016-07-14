package scalaoauth2.provider

import java.time.{ LocalDateTime, ZoneId }
import java.util.Date

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImplicitSpec extends FlatSpec with ScalaFutures with OptionValues {

  val implicitGrant = new Implicit()

  "Implicit" should "grant access with valid user authentication" in handlesRequest(implicitGrant, "user", "pass", true)
  "Implicit" should "not grant access with invalid user authentication" in handlesRequest(implicitGrant, "user", "wrong_pass", false)

  def newAccessToken(d: Date) = AccessToken("token1", Some("refresh_token"), Some("all"), Some(3600), d)

  def handlesRequest(implicitGrant: Implicit, user: String, pass: String, ok: Boolean) = {
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("client"), "username" -> Seq(user), "password" -> Seq(pass), "scope" -> Seq("all")))
    val f = implicitGrant.handleRequest(request, new MockDataHandler() {

      override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] =
        Future.successful(Some(newAccessToken(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant))))

      override def findUser(request: AuthorizationRequest): Future[Option[User]] = {
        val result = request match {
          case request: ImplicitRequest =>
            for {
              user <- request.param("username") if user == "user"
              password <- request.param("password") if password == "pass"
            } yield MockUser(10000, "username")
          case _ => None
        }
        Future.successful(result)
      }

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] =
        Future.successful(newAccessToken(new Date()))

    })

    if (ok) {
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
