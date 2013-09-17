package scalaoauth2.provider

import play.api.mvc._
import play.api.libs.json._
import scalaoauth2.provider.{Request => OAuthRequest}

trait OAuth2Provider extends Results {

  val token = Token
  val protectedResource = ProtectedResource

  implicit def play2oauthRequest[A](request: play.api.mvc.Request[A]) = {
    val form = request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined => body.asMultipartFormData.get.asFormUrlEncoded
      case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
      case body: play.api.mvc.MultipartFormData[_] => body.asFormUrlEncoded
      case _ => Map.empty[String, Seq[String]]
    }
    OAuthRequest(request.headers.toSimpleMap, form)
  }

  def issueAccessToken[A](dataHandler: DataHandler)(implicit request: play.api.mvc.Request[A]): PlainResult = {
    token.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => responseOAuthError(BadRequest, e)
      case Left(e) if e.statusCode == 401 => responseOAuthError(Unauthorized, e)
      case Right(r) => Ok(Json.toJson(responseAccessToken(r)))
    }
  }

  protected def responseAccessToken(r: GrantHandlerResult) = {
    Map[String, JsValue](
      "token_type" -> JsString(r.tokenType),
      "access_token" -> JsString(r.accessToken),
      "expires_in" -> JsNumber(r.expiresIn)
    ) ++ r.refreshToken.map {
      "refresh_token" -> JsString(_)
    } ++ r.scope.map {
      "scope" -> JsString(_)
    }
  }

  def authorize[A](dataHandler: DataHandler)(callback: AuthInfo => PlainResult)(implicit request: play.api.mvc.Request[A]): PlainResult = {
    protectedResource.handleRequest(request, dataHandler) match {
      case Left(e) if e.statusCode == 400 => responseOAuthError(BadRequest, e)
      case Left(e) if e.statusCode == 401 => responseOAuthError(Unauthorized, e)
      case Right(authInfo) => callback(authInfo)
    }
  }

  protected def responseOAuthError(result: PlainResult, e: OAuthError) = result.withHeaders(
    "WWW-Authenticate" -> ("Bearer " + toOAuthErrorString(e))
  )

  protected def toOAuthErrorString(e: OAuthError): String = {
    val params = Seq("error=\"" + e.errorType + "\"") ++
      (if (!e.description.isEmpty) { Seq("error_description=\"" + e.description + "\"") } else { Nil })
    params.mkString(", ")
  }
}
