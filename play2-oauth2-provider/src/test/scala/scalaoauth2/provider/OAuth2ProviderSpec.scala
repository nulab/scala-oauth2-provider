package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.Future
import play.api.libs.json._

class OAuth2ProviderSpec extends FlatSpec {

  object TestOAuthProvider extends OAuth2Provider {
    override def responseAccessToken(r: GrantHandlerResult) = super.responseAccessToken(r) ++ Map("custom_key" -> JsString("custom_value"))
  }

  it should "return including access token" in {
    val map = TestOAuthProvider.responseAccessToken(GrantHandlerResult(tokenType = "Bearer", accessToken = "access_token", expiresIn = Some(3600), refreshToken = None, scope = None))
    map.get("token_type") should contain (JsString("Bearer"))
    map.get("access_token") should contain (JsString("access_token"))
    map.get("expires_in") should contain (JsNumber(3600))
    map.get("refresh_token") should be (None)
    map.get("scope") should be (None)
    map.get("custom_key") should contain (JsString("custom_value"))
  }

  it should "return error message as JSON" in {
    val json = TestOAuthProvider.responseOAuthErrorJson(new InvalidRequest("request is invalid"))
    (json \ "error").as[String] should be ("invalid_request")
    (json \ "error_description").as[String] should be ("request is invalid")
  }

  it should "return error message to header" in {
    val (name, value) = TestOAuthProvider.responseOAuthErrorHeader(new InvalidRequest("request is invalid"))
    name should be ("WWW-Authenticate")
    value should be ("""Bearer error="invalid_request", error_description="request is invalid"""")
  }
}
