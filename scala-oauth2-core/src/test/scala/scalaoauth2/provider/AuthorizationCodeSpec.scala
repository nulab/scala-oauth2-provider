package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class AuthorizationCodeSpec extends FlatSpec with ScalaFutures {

  it should "handle request" in {
    val authorizationCode = new AuthorizationCode()
    val request = AuthorizationRequest(Map(), Map("code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val f = authorizationCode.handleRequest(request, Some(ClientCredential("clientId1", Some("clientSecret1"))), new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = Some("http://example.com/"))
      ))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType should be ("Bearer")
      result.accessToken should be ("token1")
      result.expiresIn should be (Some(3600))
      result.refreshToken should be (Some("refreshToken1"))
      result.scope should be (Some("all"))
    }
  }

  it should "handle request if redirectUrl is none" in {
    val authorizationCode = new AuthorizationCode()
    val request = AuthorizationRequest(Map(), Map("code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val f = authorizationCode.handleRequest(request, Some(ClientCredential("clientId1", Some("clientSecret1"))), new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None)
      ))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType should be ("Bearer")
      result.accessToken should be ("token1")
      result.expiresIn should be (Some(3600))
      result.refreshToken should be (Some("refreshToken1"))
      result.scope should be (Some("all"))
    }
  }
}
