package scalaoauth2

import play.api.mvc.Result

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object provider {

  /**
   * Support synchronous Result for Playframework
   *
   * <h3>Create controller for issue access token</h3>
   * <code>
   * import scalaoauth2.provider._
   * object OAuth2Controller extends Controller with OAuth2Provider {
   *   def accessToken = Action { implicit request =>
   *     await(issueAccessToken(new MyDataHandler()))
   *   }
   * }
   * </code>
   *
   * <h3>Register routes</h3>
   * <code>
   * POST /oauth2/access_token controllers.OAuth2Controller.accessToken
   * </code>
   *
   * <h3>Authorized</h3>
   * <code>
   * import scalaoauth2.provider._
   * object BookController extends Controller with OAuthProvider {
   *   def list = Action { implicit request =>
   *     await(authorize(new MyDataHandler()) { authInfo =>
   *       val user = authInfo.user // User is defined on your system
   *       // access resource for the user
   *       Future.successful(Ok)
   *     })
   *   }
   * }
   * </code>
   * @param f callback
   * @param timeout maximum wait time
   * @return Await and return the result.
   */
  def await(f: Future[Result], timeout: Duration = 60.seconds): Result = Await.result(f, timeout)

}
