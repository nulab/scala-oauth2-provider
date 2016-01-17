package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import scala.concurrent.Future
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.mvc.Results._

class OAuth2ProviderActionBuildersSpec extends FlatSpec {

  import OAuth2ProviderActionBuilders._

  val action = AuthorizedAction(new MockDataHandler) { request =>
    Ok(request.authInfo.user.name)
  }

  it should "return BadRequest" in {
    val result = action(FakeRequest())
    status(result) should be(400)
    contentAsString(result) should be("")
  }

}
