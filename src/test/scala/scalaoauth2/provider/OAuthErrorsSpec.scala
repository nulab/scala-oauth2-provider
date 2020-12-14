package scalaoauth2.provider

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class OAuthErrorsSpec extends AnyFlatSpec {

  behavior of "OAuth Error Handling RFC 6749 Section 5.2"

  it should "produce a 400 status code for invalid_request" in {
    new InvalidRequest().statusCode should be(400)
  }

  it should "produce a 401 status code for invalid_client" in {
    new InvalidClient().statusCode should be(401)
  }

  it should "produce a 400 status code for invalid_grant" in {
    new InvalidGrant().statusCode should be(400)
  }

  it should "produce a 400 status code for unauthorized_client" in {
    new UnauthorizedClient().statusCode should be(400)
  }

  it should "produce a 400 status code for unsupported_grant_type" in {
    new UnsupportedGrantType().statusCode should be(400)
  }

  it should "produce a 400 status code for invalid_scope" in {
    new InvalidScope().statusCode should be(400)
  }

  it should "produce a 400 status code for redirect_uri_mismatch" in {
    val error = new RedirectUriMismatch()
    error.statusCode should be(400)
    error.errorType should be("invalid_request")
  }

  behavior of "OAuth Error Handling for Bearer Tokens RFC 6750 Section 3.1"

  it should "produce a 400 status code for invalid_request" in {
    new InvalidRequest().statusCode should be(400)
  }

  it should "produce a 401 status code for invalid_token" in {
    new InvalidToken().statusCode should be(401)
  }

  it should "produce a 403 status code for insufficient_scope" in {
    new InsufficientScope().statusCode should be(403)
  }
}
