package scalaoauth2.provider

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class AuthorizationRequestSpec extends AnyFlatSpec {

  it should "fetch Basic64" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map()
    )

    request.parseClientCredential
      .fold[Option[ClientCredential]](None)(
        _.fold(_ => None, c => Some(c))
      ) should contain(
      ClientCredential("client_id_value", Some("client_secret_value"))
    )
  }

  it should "fetch Basic64 by case insensitive" in {
    val request = new AuthorizationRequest(
      Map(
        "authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map()
    )

    request.parseClientCredential
      .fold[Option[ClientCredential]](None)(
        _.fold(_ => None, c => Some(c))
      ) should contain(
      ClientCredential("client_id_value", Some("client_secret_value"))
    )
  }

  it should "fetch authorization header without colon" in {
    val request = new AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVl")),
      Map()
    )
    val parsedCred = request.parseClientCredential
    parsedCred.isDefined shouldBe true
    parsedCred.get.isLeft shouldBe true
  }

  it should "fetch empty client_secret with colon" in {
    val request = new AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOg==")),
      Map()
    )
    request.parseClientCredential
      .fold[Option[ClientCredential]](None)(
        _.fold(_ => None, c => Some(c))
      ) should contain(ClientCredential("client_id_value", None))
  }

  it should "not fetch not Authorization key in header" in {
    val request = new AuthorizationRequest(
      Map(
        "authorizatio" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map()
    )
    request.parseClientCredential should be(None)
  }

  it should "not fetch invalid Base64" in {
    val request = new AuthorizationRequest(
      Map("Authorization" -> Seq("Basic basic")),
      Map()
    )
    val parsedCred = request.parseClientCredential
    parsedCred.isDefined shouldBe true
    parsedCred.get.isLeft shouldBe true
  }

  it should "fetch parameter" in {
    val request = new AuthorizationRequest(
      Map(),
      Map(
        "client_id" -> Seq("client_id_value"),
        "client_secret" -> Seq("client_secret_value")
      )
    )

    request.parseClientCredential
      .fold[Option[ClientCredential]](None)(
        _.fold(_ => None, c => Some(c))
      ) should contain(
      ClientCredential("client_id_value", Some("client_secret_value"))
    )
  }

  it should "omit client_secret" in {
    val request = new AuthorizationRequest(
      Map(),
      Map("client_id" -> Seq("client_id_value"))
    )

    request.parseClientCredential
      .fold[Option[ClientCredential]](None)(
        _.fold(_ => None, c => Some(c))
      ) should contain(ClientCredential("client_id_value", None))
  }

  it should "not fetch missing parameter" in {
    val request = new AuthorizationRequest(
      Map(),
      Map("client_secret" -> Seq("client_secret_value"))
    )
    request.parseClientCredential should be(None)
  }

  it should "not fetch invalid parameter" in {
    val request =
      new AuthorizationRequest(Map("Authorization" -> Seq("")), Map())
    val parsedCred = request.parseClientCredential
    parsedCred.isDefined shouldBe true
    parsedCred.get.isLeft shouldBe true
  }

  it should "not fetch invalid Authorization header" in {
    val request = new AuthorizationRequest(
      Map("Authorization" -> Seq("Digest Y2xpZW50X2lkX3ZhbHVlOg==")),
      Map()
    )
    val parsedCred = request.parseClientCredential
    parsedCred.isDefined shouldBe true
    parsedCred.get.isLeft shouldBe true
  }

  it should "not fetch if Authorization header is invalid, but client_id and client_secret are valid and present in parms" in {
    val request = new AuthorizationRequest(
      Map("Authorization" -> Seq("fakeheader aaaa")),
      Map(
        "client_id" -> Seq("client_id_value"),
        "client_secret" -> Seq("client_secret_value")
      )
    )
    val parsedCred = request.parseClientCredential
    parsedCred.isDefined shouldBe true
    parsedCred.get.isLeft shouldBe true
  }
}
