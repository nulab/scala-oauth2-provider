package scalaoauth2.provider

import org.scalatest.Matchers._
import org.scalatest._

import scala.util.Success

class DataHandlerSpec extends FlatSpec with TryValues {

  it should "parse a PKCE code challenge method of plain" in {
    CodeChallengeMethod("plain") should be(Success(Plain))
  }

  it should "parse a PKCE code challenge method of S256" in {
    CodeChallengeMethod("S256") should be(Success(S256))
  }

  it should "return a failure if non valid PKCE code challenge method" in {
    val attempt = CodeChallengeMethod("made-up")
    attempt.isFailure shouldBe true
    attempt.failure.exception.isInstanceOf[InvalidRequest]
    attempt.failure.exception.getMessage should be("transform algorithm not supported")
  }
}
