package scalaoauth2.provider

import org.scalatest.{ FlatSpec, OptionValues }
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RefreshTokenSpec extends FlatSpec with ScalaFutures with OptionValues {

  it should "handle request" in {
    val refreshToken = new RefreshToken()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "clinet_secret" -> Seq("clientSecret1"), "refresh_token" -> Seq("refreshToken1")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = refreshToken.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] =
        Future.successful(Some(AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = None, redirectUri = None)))

      override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("token1", Some(refreshToken), None, Some(3600), new java.util.Date()))

    })

    whenReady(f) { result =>
      result.tokenType should be("Bearer")
      result.accessToken should be("token1")
      result.expiresIn.value should (be <= 3600L and be > 3595L)
      result.refreshToken should be(Some("refreshToken1"))
      result.scope should be(None)
    }
  }
}
