package scalaoauth2.provider

abstract class OAuthError(val statusCode: Int, val description: String) extends Exception {

  def this(description: String) = this(400, description)

  val errorType: String

}

class InvalidRequest(description: String = "") extends OAuthError(description) {

  override val errorType = "invalid_request"

}

class InvalidClient(description: String = "") extends OAuthError(401, description) {

  override val errorType = "invalid_client"

}

class UnauthorizedClient(description: String = "") extends OAuthError(401, description) {

  override val errorType = "unauthorized_client"

}

class RedirectUriMismatch(description: String = "") extends OAuthError(401, description) {

  override val errorType = "redirect_uri_mismatch"

}

class AccessDenied(description: String = "") extends OAuthError(401, description) {

  override val errorType = "access_denied"

}

class UnsupportedResponseType(description: String = "") extends OAuthError(description) {

  override val errorType = "unsupported_response_type"

}

class InvalidGrant(description: String = "") extends OAuthError(401, description) {

  override val errorType = "invalid_grant"

}

class UnsupportedGrantType(description: String = "") extends OAuthError(description) {

  override val errorType = "unsupported_grant_type"

}

class InvalidScope(description: String = "") extends OAuthError(401, description) {

  override val errorType = "invalid_scope"

}

class InvalidToken(description: String = "") extends OAuthError(401, description) {

  override val errorType = "invalid_token"

}

class ExpiredToken() extends OAuthError(401, "The access token expired") {

  override val errorType = "invalid_token"

}

class InsufficientScope(description: String = "") extends OAuthError(401, description) {

  override  val errorType = "insufficient_scope"

}
