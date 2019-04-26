package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest.FlatSpec

class RequestParameterSpec extends FlatSpec {

  def createRequest(oauthToken: Option[String], accessToken: Option[String], another: Map[String, Seq[String]] = Map()): ProtectedResourceRequest = {
    val params = oauthToken.map { "oauth_token" -> Seq(_) } ++ accessToken.map { "access_token" -> Seq(_) }
    new ProtectedResourceRequest(Map(), Map() ++ params ++ another)
  }

  it should "match RequestParameter" in {
    RequestParameter.matches(createRequest(Some("token1"), None)) should be(true)
    RequestParameter.matches(createRequest(None, Some("token2"))) should be(true)
    RequestParameter.matches(createRequest(Some("token1"), Some("token2"))) should be(true)
  }

  it should "doesn't match RequestParameter" in {
    RequestParameter.matches(createRequest(None, None)) should be(false)
  }

  it should "fetch only oauth token parameter" in {
    val result = RequestParameter.fetch(createRequest(Some("token1"), None))
    result.token should be("token1")
    result.params should be(Symbol("empty"))
  }

  it should "fetch only access token parameter" in {
    val result = RequestParameter.fetch(createRequest(None, Some("token2")))
    result.token should be("token2")
    result.params should be(Symbol("empty"))
  }

  it should "fetch with another parameter" in {
    val result = RequestParameter.fetch(createRequest(None, Some("token2"), Map("foo" -> Seq("bar"))))
    result.token should be("token2")
    result.params.get("foo") should be(Some("bar"))
  }

  it should "fetch illegal parameter then throws exception" in {
    intercept[InvalidRequest] {
      RequestParameter.fetch(createRequest(None, None))
    }
  }
}
