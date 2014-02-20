package scalaoauth2.provider

case class AuthorizationRequest(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) extends RequestBase(headers, params) {

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
  def grantType: Option[String] = param("grant_type")

  /**
   * Returns client_id.
   * 
   * @return client_id
   */
  def clientId: Option[String] = param("client_id")

  /**
   * Returns client_secret.
   * 
   * @return client_secret
   */
  def clientSecret: Option[String] = param("client_secret")

  /**
   * Returns scope.
   * 
   * @return scope
   */
  def scope: Option[String] = param("scope")

  /**
   * Returns redirect_uri.
   * 
   * @return redirect_uri
   */
  def redirectUri: Option[String] = param("redirect_uri")

  /**
   * returns code.
   * 
   * @return code.
   * @throws InvalidRequest If not found code
   */
  def requireCode: String = requireParam("code")

  /**
   * returns username.
   *
   * @return username.
   * @throws InvalidRequest If not found username
   */
  def requireUsername: String = requireParam("username")

  /**
   * returns password.
   *
   * @return code.
   * @throws InvalidRequest If not found password
   */
  def requirePassword: String = requireParam("password")

  /**
   * returns refresh_token.
   *
   * @return code.
   * @throws InvalidRequest If not found refresh_token
   */
  def requireRefreshToken: String = requireParam("refresh_token")
}
