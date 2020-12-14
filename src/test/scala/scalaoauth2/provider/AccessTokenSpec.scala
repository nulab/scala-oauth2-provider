package scalaoauth2.provider

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.Date
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class AccessTokenSpec extends AnyFlatSpec {

  it should "say a token is active that is not yet expired" in {
    val token = AccessToken(
      "token",
      None,
      None,
      lifeSeconds = Some(15),
      createdAt = new Date()
    )
    token.isExpired shouldBe false
  }

  it should "expire tokens that have a lifespan that has passed" in {
    val token = AccessToken(
      "token",
      None,
      None,
      lifeSeconds = Some(1798),
      createdAt = Date.from(
        ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(1800).toInstant
      )
    )
    token.isExpired shouldBe true
  }

  it should "not expire tokens that have no lifespan" in {
    val token = AccessToken(
      "token",
      None,
      None,
      lifeSeconds = None,
      createdAt = Date.from(
        ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(1800).toInstant
      )
    )
    token.isExpired shouldBe false
  }
}
