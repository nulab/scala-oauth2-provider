package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class TokenSpec extends FlatSpec {

  def successfulDataHandler() = new MockDataHandler() {

    override def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean = true

    override def findUser(username: String, password: String): Option[MockUser] = Some(MockUser(10000, "username"))

    override def createOrUpdateAuthInfo(user: MockUser, clientId: String, scope: Option[String]): Option[AuthInfo[MockUser]] = Some(
      AuthInfo(id = "1", user = user, clientId = clientId, refreshToken = Some("refreshToken1"), scope = scope, code = Some("code1"), redirectUri = Some("http://example.com/"))
    )

    override def createOrUpdateAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("authId1", "token1", 3600, new java.util.Date())

  }

  it should "be handled request" in {
    val request = Request(
      Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    Token.handleRequest(request, dataHandler) should be ('right)
  }

  it should "be error if grant type doesn't exist" in {
    val request = Request(
      Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    intercept[InvalidRequest] {
      Token.handleRequest(request, dataHandler) match {
        case Left(e) => throw e
        case _ =>
      }
    }
  }

  it should "be error if grant type is wrong" in {
    val request = Request(
      Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="),
      Map("grant_type" -> Seq("test"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    intercept[UnsupportedGrantType] {
      Token.handleRequest(request, dataHandler) match {
        case Left(e) => throw e
        case _ =>
      }
    }
  }

  it should "be invalid request without client credential" in {
    val request = Request(
      Map(),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    intercept[InvalidRequest] {
      Token.handleRequest(request, dataHandler) match {
        case Left(e) => throw e
        case _ =>
      }
    }
  }

  it should "be invalid client if client information is wrong" in {
    val request = Request(
      Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = new MockDataHandler() {

      override def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean = false

    }

    intercept[InvalidClient] {
      Token.handleRequest(request, dataHandler) match {
        case Left(e) => throw e
        case _ =>
      }
    }
  }
}
