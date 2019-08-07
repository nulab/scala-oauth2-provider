package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AuthorizationCodeSpec extends FlatSpec with ScalaFutures with OptionValues {

  it should "handle request" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "client_secret" -> Seq("clientSecret1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    var codeDeleted: Boolean = false
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = Some("http://example.com/"))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))

      override def deleteAuthCode(code: String): Future[Unit] = {
        Thread.sleep(300)
        codeDeleted = true
        Future.successful(())
      }
    })

    whenReady(f, timeout(Span(1, Seconds)), interval(Span(50, Millis))) { result =>
      codeDeleted shouldBe true
      result.tokenType shouldBe "Bearer"
      result.accessToken shouldBe "token1"
      result.expiresIn.value should (be <= 3600L and be > 3595L)
      result.refreshToken shouldBe Some("refreshToken1")
      result.scope shouldBe Some("all")
    }
  }

  it should "handle request if redirectUrl is none" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "client_secret" -> Seq("clientSecret1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None)))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType shouldBe "Bearer"
      result.accessToken shouldBe "token1"
      result.expiresIn.value should (be <= 3600L and be > 2595L)
      result.refreshToken shouldBe Some("refreshToken1")
      result.scope shouldBe Some("all")
    }
  }

  it should "handle a PKCE plain (implicit) request" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/"), "code_verifier" -> Seq("4g94A5mKbcP1zv313x6JmVQjDJ1FiwVFBnLepwk1BLk")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None, codeChallenge = Some("4g94A5mKbcP1zv313x6JmVQjDJ1FiwVFBnLepwk1BLk"))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType shouldBe "Bearer"
      result.accessToken shouldBe "token1"
      result.expiresIn.value should (be <= 3600L and be > 2595L)
      result.refreshToken shouldBe Some("refreshToken1")
      result.scope shouldBe Some("all")
    }
  }

  it should "handle a PKCE plain (explicit) request" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/"), "code_verifier" -> Seq("4g94A5mKbcP1zv313x6JmVQjDJ1FiwVFBnLepwk1BLk")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None, codeChallenge = Some("4g94A5mKbcP1zv313x6JmVQjDJ1FiwVFBnLepwk1BLk"), codeChallengeMethod = Some(Plain))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType shouldBe "Bearer"
      result.accessToken shouldBe "token1"
      result.expiresIn.value should (be <= 3600L and be > 2595L)
      result.refreshToken shouldBe Some("refreshToken1")
      result.scope shouldBe Some("all")
    }
  }

  it should "handle a PKCE S256 request" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/"), "code_verifier" -> Seq("4g94A5mKbcP1zv313x6JmVQjDJ1FiwVFBnLepwk1BLk")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None, codeChallenge = Some("aLE640XFc4YAPoVB3KCUhcjoMRDQhyILWy9k9qpiBfo"), codeChallengeMethod = Some(S256))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f) { result =>
      result.tokenType shouldBe "Bearer"
      result.accessToken shouldBe "token1"
      result.expiresIn.value should (be <= 3600L and be > 2595L)
      result.refreshToken shouldBe Some("refreshToken1")
      result.scope shouldBe Some("all")
    }
  }

  it should "return a InvalidGrant if PKCE validation fails for equality" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/"), "code_verifier" -> Seq("iB3OM4lYP6k03yT_sMGr1o_Mf-lOX84mrM7jRkm21Ak")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None, codeChallenge = Some("aLE640XFc4YAPoVB3KCUhcjoMRDQhyILWy9k9qpiBfo"), codeChallengeMethod = Some(S256))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f.failed) { e =>
      e shouldBe a[InvalidGrant]
    }
  }

  it should "return a InvalidGrant if code_verifier is not included when AuthInfo contains a codeChallenge" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None, codeChallenge = Some("aLE640XFc4YAPoVB3KCUhcjoMRDQhyILWy9k9qpiBfo"))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))
    })

    whenReady(f.failed) { e =>
      e shouldBe a[InvalidGrant]
    }
  }

  it should "return a Failure Future" in {
    val authorizationCode = new AuthorizationCode()
    val request = new AuthorizationRequest(Map(), Map("client_id" -> Seq("clientId1"), "client_secret" -> Seq("clientSecret1"), "code" -> Seq("code1"), "redirect_uri" -> Seq("http://example.com/")))
    val clientCred = request.parseClientCredential.fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = authorizationCode.handleRequest(clientCred, request, new MockDataHandler() {

      override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = Some("http://example.com/"))))

      override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new java.util.Date()))

      override def deleteAuthCode(code: String): Future[Unit] = {
        Future.failed(new Exception())
      }
    })

    whenReady(f.failed) { e =>
      e shouldBe a[Exception]
    }
  }
}
