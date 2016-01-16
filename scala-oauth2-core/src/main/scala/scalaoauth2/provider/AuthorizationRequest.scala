package scalaoauth2.provider

import org.apache.commons.codec.binary.Base64

case class ClientCredential(clientId: String, clientSecret: Option[String])

class AuthorizationRequest(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) extends RequestBase(headers, params) {

  /**
   * Returns grant_type.
   * 
   * OAuth defines four grant types: 
   * authorization code
   * implicit
   * resource owner password credentials, and client credentials.
   * 
   * @return grant_type
   */
  def grantType: String = requireParam("grant_type")

  /**
   * Returns scope.
   * 
   * @return scope
   */
  def scope: Option[String] = param("scope")

  lazy val clientCredential: Option[ClientCredential] = {
    header("Authorization").flatMap {
      """^\s*Basic\s+(.+?)\s*$""".r.findFirstMatchIn
    } match {
      case Some(matcher) =>
        val authorization = matcher.group(1)
        val decoded = new String(Base64.decodeBase64(authorization.getBytes), "UTF-8")
        if (decoded.indexOf(':') > 0) {
          decoded.split(":", 2) match {
            case Array(clientId, clientSecret) => Some(ClientCredential(clientId, if (clientSecret == "") None else Some(clientSecret)))
            case Array(clientId) => Some(ClientCredential(clientId, None))
          }
        } else {
          None
        }
      case _ => param("client_id").map { clientId =>
        ClientCredential(clientId, param("client_secret"))
      }
    }
  }
}

case class RefreshTokenRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
    * returns refresh_token.
    *
    * @return code.
    * @throws InvalidRequest if the parameter is not found
    */
  def refreshToken: String = requireParam("refresh_token")

  override lazy val clientCredential = request.clientCredential
}

case class PasswordRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
    * returns username.
    *
    * @return username.
    * @throws InvalidRequest if the parameter is not found
    */
  def username = requireParam("username")

  /**
    * returns password.
    *
    * @return password.
    * @throws InvalidRequest if the parameter is not found
    */
  def password = requireParam("password")

  override lazy val clientCredential = request.clientCredential
}

case class ClientCredentialsRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  override lazy val clientCredential = request.clientCredential
}

case class AuthorizationCodeRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
    * returns code.
    *
    * @return code.
    * @throws InvalidRequest if code is not found
    */
  def code: String = requireParam("code")

  /**
    * Returns redirect_uri.
    *
    * @return redirect_uri
    */
  def redirectUri: Option[String] = param("redirect_uri")

  override lazy val clientCredential = request.clientCredential

}

case class ImplicitRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  override lazy val clientCredential = request.clientCredential
}
