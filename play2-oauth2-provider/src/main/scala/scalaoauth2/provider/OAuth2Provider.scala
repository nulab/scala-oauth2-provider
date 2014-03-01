package scalaoauth2.provider

import play.api.mvc._
import play.api.libs.json._
import scala.language.implicitConversions

/**
 * OAuth2Provider supports issue access token and authorize.
 *
 * <h3>Create controller for issue access token</h3>
 * <code>
 * object OAuth2Controller extends Controller with OAuth2Provider {
 *   def accessToken = Action { implicit request =>
 *     issueAccessToken(new MyDataHandler())
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
 *     authorize(new MyDataHandler()) { authInfo =>
 *       val user = authInfo.user // User is defined on your system
 *       // access resource for the user
 *     }
 *   }
 * }
 * </code>
 */
trait OAuth2Provider extends Results {

  implicit def play2oauthRequest(request: RequestHeader): AuthorizationRequest = {
    AuthorizationRequest(request.headers.toMap, request.queryString)
  }

  implicit def play2oauthRequest[A](request: play.api.mvc.Request[A]): AuthorizationRequest = {
    val param: Map[String, Seq[String]] = getParam(request)
    AuthorizationRequest(request.headers.toMap, param)
  }
  
  implicit def play2protectedResourceRequest[A](request: play.api.mvc.Request[A]): ProtectedResourceRequest = {
    val param: Map[String, Seq[String]] = getParam(request)
    ProtectedResourceRequest(request.headers.toMap, param)
  }

  final def getParam[A](request: Request[A]): Map[String, Seq[String]] = {
    val form = request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined => body.asMultipartFormData.get.asFormUrlEncoded
      case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
      case body: play.api.mvc.MultipartFormData[_] => body.asFormUrlEncoded
      case _ => Map.empty[String, Seq[String]]
    }

    form ++ request.queryString.map {
      case (k, v) => k -> (v ++ form.getOrElse(k, Nil))
    }
  }

  /**
   * Issue access token in DataHandler process and return the response to client.
   *
   * @param dataHandler Implemented DataHander for register access token to your system.
   * @param request Playframework is provided HTTP request interface.
   * @tparam A play.api.mvc.Request has type.
   * @return Request is successful then return JSON to client in OAuth 2.0 format.
   *         Request is failed then return BadRequest or Unauthorized status to client with cause into the JSON.
   */
  def issueAccessToken[A, U](dataHandler: DataHandler[U])(implicit request: play.api.mvc.Request[A]): SimpleResult = {
    TokenEndpoint.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => responseOAuthError(BadRequest, e)
      case Left(e) if e.statusCode == 401 => responseOAuthError(Unauthorized, e)
      case Right(r) => Ok(Json.toJson(responseAccessToken(r)))
    }
  }

  protected def responseAccessToken(r: GrantHandlerResult) = {
    Map[String, JsValue](
      "token_type" -> JsString(r.tokenType),
      "access_token" -> JsString(r.accessToken)
    ) ++ r.expiresIn.map {
      "expires_in" -> JsNumber(_)
    } ++ r.refreshToken.map {
      "refresh_token" -> JsString(_)
    } ++ r.scope.map {
      "scope" -> JsString(_)
    }
  }

  /**
   * Authorize to already created access token in DataHandler process and return the response to client.
   *
   * @param dataHandler Implemented DataHander for authenticate to your system.
   * @param callback Callback is called when authentication is successful.
   * @param request Playframework is provided HTTP request interface.
   * @tparam A play.api.mvc.Request has type.
   * @return Authentication is successful then the response use your API result.
   *         Authentication is failed then return BadRequest or Unauthorized status to client with cause into the JSON.
   */
  def authorize[A, U](dataHandler: DataHandler[U])(callback: AuthInfo[U] => SimpleResult)(implicit request: play.api.mvc.Request[A]): SimpleResult = {
    ProtectedResource.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => responseOAuthError(BadRequest, e)
      case Left(e) if e.statusCode == 401 => responseOAuthError(Unauthorized, e)
      case Right(authInfo) => callback(authInfo)
    }
  }

  protected def responseOAuthError(result: SimpleResult, e: OAuthError) = result.withHeaders(
    "WWW-Authenticate" -> ("Bearer " + toOAuthErrorString(e))
  )

  protected def toOAuthErrorString(e: OAuthError): String = {
    val params = Seq("error=\"" + e.errorType + "\"") ++
      (if (!e.description.isEmpty) { Seq("error_description=\"" + e.description + "\"") } else { Nil })
    params.mkString(", ")
  }
}
