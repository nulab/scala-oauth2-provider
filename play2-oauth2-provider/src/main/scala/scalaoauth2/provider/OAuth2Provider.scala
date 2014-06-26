package scalaoauth2.provider

import play.api.mvc._
import play.api.libs.json._
import scala.language.implicitConversions

/**
 * Basic OAuth2 provider trait.
 */
trait OAuth2BaseProvider extends Results {

  implicit def play2oauthRequest(request: RequestHeader): AuthorizationRequest = {
    AuthorizationRequest(request.headers.toMap, request.queryString)
  }

  implicit def play2oauthRequest[A](request: Request[A]): AuthorizationRequest = {
    val param: Map[String, Seq[String]] = getParam(request)
    AuthorizationRequest(request.headers.toMap, param)
  }

  implicit def play2protectedResourceRequest(request: RequestHeader): ProtectedResourceRequest = {
    ProtectedResourceRequest(request.headers.toMap, request.queryString)
  }

  implicit def play2protectedResourceRequest[A](request: Request[A]): ProtectedResourceRequest = {
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

  protected[scalaoauth2] def responseAccessToken(r: GrantHandlerResult) = {
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

  protected[scalaoauth2] def responseOAuthErrorJson(e: OAuthError): JsValue = Json.obj(
    "error" -> e.errorType,
    "error_description" -> e.description
  )

  protected[scalaoauth2] def responseOAuthErrorHeader(e: OAuthError): (String, String) = ("WWW-Authenticate" -> ("Bearer " + toOAuthErrorString(e)))

  protected def toOAuthErrorString(e: OAuthError): String = {
    val params = Seq("error=\"" + e.errorType + "\"") ++
      (if (!e.description.isEmpty) { Seq("error_description=\"" + e.description + "\"") } else { Nil })
    params.mkString(", ")
  }

}

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
trait OAuth2Provider extends OAuth2BaseProvider {

  /**
   * Issue access token in DataHandler process and return the response to client.
   *
   * @param dataHandler Implemented DataHander for register access token to your system.
   * @param request Playframework is provided HTTP request interface.
   * @tparam A play.api.mvc.Request has type.
   * @return Request is successful then return JSON to client in OAuth 2.0 format.
   *         Request is failed then return BadRequest or Unauthorized status to client with cause into the JSON.
   */
  def issueAccessToken[A, U](dataHandler: DataHandler[U])(implicit request: play.api.mvc.Request[A]): Result = {
    TokenEndpoint.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => BadRequest(responseOAuthErrorJson(e)).withHeaders(responseOAuthErrorHeader(e))
      case Left(e) if e.statusCode == 401 => Unauthorized(responseOAuthErrorJson(e)).withHeaders(responseOAuthErrorHeader(e))
      case Right(r) => Ok(Json.toJson(responseAccessToken(r)))
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
  def authorize[A, U](dataHandler: DataHandler[U])(callback: AuthInfo[U] => Result)(implicit request: play.api.mvc.Request[A]): Result = {
    ProtectedResource.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => BadRequest.withHeaders(responseOAuthErrorHeader(e))
      case Left(e) if e.statusCode == 401 => Unauthorized.withHeaders(responseOAuthErrorHeader(e))
      case Right(authInfo) => callback(authInfo)
    }
  }

}

/**
 * OAuth2AsyncProvider supports issue access token and authorize in asynchronous.
 *
 * <h3>Create controller for issue access token</h3>
 * <code>
 * object OAuth2Controller extends Controller with OAuth2AsyncProvider {
 *   def accessToken = Action.async { implicit request =>
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
 * object BookController extends Controller with OAuth2AsyncProvider {
 *   def list = Action.async { implicit request =>
 *     authorize(new MyDataHandler()) { authInfo =>
 *       val user = authInfo.user // User is defined on your system
 *       // access resource for the user
 *     }
 *   }
 * }
 * </code>
 */
trait OAuth2AsyncProvider extends OAuth2BaseProvider {

  import scala.concurrent.Future

  /**
   * Issue access token in DataHandler process and return the response to client.
   *
   * @param dataHandler Implemented DataHander for register access token to your system.
   * @param request Playframework is provided HTTP request interface.
   * @tparam A play.api.mvc.Request has type.
   * @return Request is successful then return JSON to client in OAuth 2.0 format.
   *         Request is failed then return BadRequest or Unauthorized status to client with cause into the JSON.
   */
  def issueAccessToken[A, U](dataHandler: DataHandler[U])(implicit request: play.api.mvc.Request[A]): Future[Result] = {
    TokenEndpoint.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => Future.successful(BadRequest(responseOAuthErrorJson(e)).withHeaders(responseOAuthErrorHeader(e)))
      case Left(e) if e.statusCode == 401 => Future.successful(Unauthorized(responseOAuthErrorJson(e)).withHeaders(responseOAuthErrorHeader(e)))
      case Right(r) => Future.successful(Ok(Json.toJson(responseAccessToken(r))))
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
  def authorize[A, U](dataHandler: DataHandler[U])(callback: AuthInfo[U] => Future[Result])(implicit request: play.api.mvc.Request[A]): Future[Result] = {
    ProtectedResource.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => Future.successful(BadRequest.withHeaders(responseOAuthErrorHeader(e)))
      case Left(e) if e.statusCode == 401 => Future.successful(Unauthorized.withHeaders(responseOAuthErrorHeader(e)))
      case Right(authInfo) => callback(authInfo)
    }
  }

}
