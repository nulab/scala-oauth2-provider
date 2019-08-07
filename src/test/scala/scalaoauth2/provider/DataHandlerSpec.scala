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

  it should "turn a PKCE code challenge method type of S256 back to a string value of S256" in {
    val method: CodeChallengeMethod = S256
    method.toString should be("S256")
  }

  it should "turn a PKCE code challenge method type of plain back to a string value of plain" in {
    val method: CodeChallengeMethod = Plain
    method.toString should be("plain")
  }

  it should "return a failure if non valid PKCE code challenge method" in {
    val attempt = CodeChallengeMethod("made-up")
    attempt.isFailure shouldBe true
    attempt.failure.exception.isInstanceOf[InvalidRequest]
    attempt.failure.exception.getMessage should be("transform algorithm not supported")
  }
}
