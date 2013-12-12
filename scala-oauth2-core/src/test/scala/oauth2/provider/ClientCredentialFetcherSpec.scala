package scalaoauth2.provider

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers._

class ClientCredentialFetcherSpec extends FlatSpec {

  it should "fetch Basic64" in {
    val request = AuthorizationRequest(Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="), Map())
    val Some(c) = ClientCredentialFetcher.fetch(request)
    c.clientId should be ("client_id_value")
    c.clientSecret should be ("client_secret_value")
  }
  
  it should "fetch empty client_secret" in {
    val request = AuthorizationRequest(Map("Authorization" -> "Basic Y2xpZW50X2lkX3ZhbHVlOg=="), Map())
    val Some(c) = ClientCredentialFetcher.fetch(request)
    c.clientId should be ("client_id_value")
    c.clientSecret should be ("")
  }

  it should "not fetch no Authorization key in header" in {
    val request = AuthorizationRequest(Map("authorizatio" -> "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="), Map())
    ClientCredentialFetcher.fetch(request) should be (None)
  }

  it should "not fetch invalidate Base64" in {
    val request = AuthorizationRequest(Map("Authorization" -> "Basic basic"), Map())
    ClientCredentialFetcher.fetch(request) should be (None)
  }

  it should "fetch parameter" in {
    val request = AuthorizationRequest(Map(), Map("client_id" -> Seq("client_id_value"), "client_secret" -> Seq("client_secret_value")))
    val Some(c) = ClientCredentialFetcher.fetch(request)
    c.clientId should be ("client_id_value")
    c.clientSecret should be ("client_secret_value")
  }

  it should "omit client_secret" in {
    val Some(c) = ClientCredentialFetcher.fetch(AuthorizationRequest(Map(), Map("client_id" -> Seq("client_id_value"))))
    c.clientId should be ("client_id_value")
    c.clientSecret should be ("")
  }
  
  it should "not fetch missing parameter" in {
    ClientCredentialFetcher.fetch(AuthorizationRequest(Map(), Map("client_secret" -> Seq("client_secret_value")))) should be (None)
  }
}
