package scalaoauth2.provider

import java.time.{ ZoneOffset, ZonedDateTime }
import java.util.Date

import org.scalatest.Matchers._
import org.scalatest._

class AccessTokenSpec extends FlatSpec {

  it should "say a token is active that is not yet expired" in {
    val token = AccessToken("token", None, None, Some(15), new Date())
    token.isExpired shouldBe false
  }

  it should "expire tokens that have a lifespan that has passed" in {
    val token = AccessToken("token", None, None, Some(1798), Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(30).toInstant))
    token.isExpired shouldBe true
  }

  it should "not expire tokens that have no lifespan" in {
    val token = AccessToken("token", None, None, None, Date.from(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(30).toInstant))
    token.isExpired shouldBe false
  }
}
