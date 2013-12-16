package scalaoauth2.provider

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers._

class AuthHeaderSpec extends FlatSpec {

  def createRequest(authorization: Option[String]): ProtectedResourceRequest = authorization match {
    case Some(s) => ProtectedResourceRequest(Map("Authorization" -> s), Map())
    case _ => ProtectedResourceRequest(Map(), Map())
  }

  it should "match AuthHeader from OAuth" in {
    AuthHeader.matches(createRequest(Some("OAuth token1"))) should be (true)
    AuthHeader.matches(createRequest(Some(" OAuth token1 "))) should be (true)
  }

  it should "match AuthHeader from Bearer" in {
    AuthHeader.matches(createRequest(Some("Bearer token1"))) should be (true)
    AuthHeader.matches(createRequest(Some(" Bearer token1 "))) should be (true)
  }

  it should "doesn't match AuthHeader from OAuth" in {
    AuthHeader.matches(createRequest(None)) should be (false)
    AuthHeader.matches(createRequest(Some("OAuth"))) should be (false)
    AuthHeader.matches(createRequest(Some("OAtu token1"))) should be (false)
    AuthHeader.matches(createRequest(Some("oauth token1"))) should be (false)
  }

  it should "doesn't match AuthHeader from Bearer" in {
    AuthHeader.matches(createRequest(None)) should be (false)
    AuthHeader.matches(createRequest(Some("Bearer"))) should be (false)
    AuthHeader.matches(createRequest(Some("Beare token1"))) should be (false)
    AuthHeader.matches(createRequest(Some("bearer token1"))) should be (false)
  }

  it should "fetch parameter from OAuth" in {
    val result = AuthHeader.fetch(createRequest(Some("""OAuth access_token_value,algorithm="hmac-sha256",nonce="s8djwd",signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D",timestamp="137131200"""")))
    result.token should be ("access_token_value")
    result.params("algorithm") should be ("hmac-sha256")
    result.params("nonce") should be ("s8djwd")
    result.params("signature") should be ("wOJIO9A2W5mFwDgiDvZbTSMK/PY=")
    result.params("timestamp") should be ("137131200")
  }

  it should "fetch parameter from Bearer" in {
    val result = AuthHeader.fetch(createRequest(Some("""Bearer access_token_value,algorithm="hmac-sha256",nonce="s8djwd",signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D",timestamp="137131200"""")))
    result.token should be ("access_token_value")
    result.params("algorithm") should be ("hmac-sha256")
    result.params("nonce") should be ("s8djwd")
    result.params("signature") should be ("wOJIO9A2W5mFwDgiDvZbTSMK/PY=")
    result.params("timestamp") should be ("137131200")
  }

  it should "fetch illegal parameter then throws exception" in {
    intercept[InvalidRequest] {
      AuthHeader.fetch(createRequest(None))
    }

    intercept[InvalidRequest] {
      AuthHeader.fetch(createRequest(Some("evil")))
    }
  }

}
