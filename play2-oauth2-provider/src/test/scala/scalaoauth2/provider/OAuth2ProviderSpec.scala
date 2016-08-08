package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._
import play.api.libs.json._
import play.api.mvc.{ AnyContentAsFormUrlEncoded, AnyContentAsJson }
import play.api.test.{ FakeHeaders, FakeRequest }

class OAuth2ProviderSpec extends FlatSpec {

  case class User(id: Long, name: String)

  object TestOAuthProvider extends OAuth2Provider {
    override def responseAccessToken[U](r: GrantHandlerResult[U]) = super.responseAccessToken(r) ++ Map("custom_key" -> JsString("custom_value"))
  }

  it should "return including access token" in {
    val map = TestOAuthProvider.responseAccessToken(
      GrantHandlerResult(
        authInfo = AuthInfo[User](user = User(0L, "name"), Some("client_id"), None, None),
        tokenType = "Bearer",
        accessToken = "access_token",
        expiresIn = Some(3600),
        refreshToken = None,
        scope = None,
        params = Map.empty
      )
    )
    map.get("token_type") should contain(JsString("Bearer"))
    map.get("access_token") should contain(JsString("access_token"))
    map.get("expires_in") should contain(JsNumber(3600))
    map.get("refresh_token") should be(None)
    map.get("scope") should be(None)
    map.get("custom_key") should contain(JsString("custom_value"))
  }

  it should "return error message as JSON" in {
    val json = TestOAuthProvider.responseOAuthErrorJson(new InvalidRequest("request is invalid"))
    (json \ "error").as[String] should be("invalid_request")
    (json \ "error_description").as[String] should be("request is invalid")
  }

  it should "return error message to header" in {
    val (name, value) = TestOAuthProvider.responseOAuthErrorHeader(new InvalidRequest("request is invalid"))
    name should be("WWW-Authenticate")
    value should be("""Bearer error="invalid_request", error_description="request is invalid"""")
  }

  it should "get parameters from form url encoded body" in {
    val values = Map(
      "id" -> List("1000"),
      "language" -> List("Scala")
    )
    val request = FakeRequest(method = "GET", uri = "/", headers = FakeHeaders(), body = AnyContentAsFormUrlEncoded(values))
    val params = TestOAuthProvider.getParam(request)
    params.get("id") should contain(List("1000"))
    params.get("language") should contain(List("Scala"))
  }

  it should "get parameters from query string" in {
    val values = Map(
      "id" -> List("1000"),
      "language" -> List("Scala")
    )
    val request = FakeRequest(method = "GET", uri = "/?version=2.11", headers = FakeHeaders(), body = AnyContentAsFormUrlEncoded(values))
    val params = TestOAuthProvider.getParam(request)
    params.get("id") should contain(List("1000"))
    params.get("language") should contain(List("Scala"))
    params.get("version") should contain(List("2.11"))
  }

  it should "get parameters from JSON body" in {
    val json = Json.obj(
      "id" -> 1000,
      "language" -> "Scala"
    )
    val request = FakeRequest(method = "GET", uri = "/", headers = FakeHeaders(), body = AnyContentAsJson(json))
    val params = TestOAuthProvider.getParam(request)
    params.get("id") should contain(List("1000"))
    params.get("language") should contain(List("Scala"))
  }
}
