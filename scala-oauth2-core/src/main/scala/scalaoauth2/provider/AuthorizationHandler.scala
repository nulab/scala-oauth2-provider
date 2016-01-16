package scalaoauth2.provider

import scala.concurrent.Future

/**
 * Provide <b>Authorization</b> phases support for using OAuth 2.0.
 *
 * <h3>[Authorization phases]</h3>
 *
 * <h4>Authorization Code Grant</h4>
 * <ul>
 *   <li>validateClient(request)</li>
 *   <li>findAuthInfoByCode(code)</li>
 *   <li>deleteAuthCode(code)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>refreshAccessToken(authInfo, token)</li>
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 *
 * <h4>Refresh Token Grant</h4>
 * <ul>
 *   <li>validateClient(clientCredential, grantType)</li>
 *   <li>findAuthInfoByRefreshToken(refreshToken)</li>
 *   <li>refreshAccessToken(authInfo, refreshToken)</li>
 * </ul>
 *
 * <h4>Resource Owner Password Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(request)</li>
 *   <li>findUser(request)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>refreshAccessToken(authInfo, token)</li>
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 *
 * <h4>Client Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(request)</li>
 *   <li>findUser(request)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>refreshAccessToken(authInfo, token)</li>
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 *
 * <h4>Implicit Grant</h4>
 * <ul>
 *   <li>validateClient(request)</li>
 *   <li>findUser(request)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 *
 */
trait AuthorizationHandler[U] {

  /**
   * Verify proper client with parameters for issue an access token.
   *
   * @param request Request sent by client.
   * @return true if request is a regular client, false if request is a illegal client.
   */
  def validateClient(request: AuthorizationRequest): Future[Boolean]

  /**
   * Authenticate the user that issued the authorization request.
   * Client credential, Password and Implicit Grant call this method.
   *
   * @param request Request sent by client.
   */
  def findUser(request: AuthorizationRequest): Future[Option[U]]

  /**
   * Creates a new access token by authorized information.
   *
   * @param authInfo This value is already authorized by system.
   * @return Access token returns to client.
   */
  def createAccessToken(authInfo: AuthInfo[U]): Future[AccessToken]

  /**
   * Returns stored access token by authorized information.
   *
   * If want to create new access token then have to return None
   *
   * @param authInfo This value is already authorized by system.
   * @return Access token returns to client.
   */
  def getStoredAccessToken(authInfo: AuthInfo[U]): Future[Option[AccessToken]]

  /**
   * Creates a new access token by refreshToken.
   *
   * @param authInfo This value is already authorized by system.
   * @return Access token returns to client.
   */
  def refreshAccessToken(authInfo: AuthInfo[U], refreshToken: String): Future[AccessToken]

  /**
   * Find authorized information by authorization code.
   *
   * If you don't support Authorization Code Grant then doesn't need implementing.
   *
   * @param code Client sends authorization code which is registered by system.
   * @return Return authorized information that matched the code.
   */
  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[U]]]

  /**
   * Deletes an authorization code.
   *
   * Called when an AccessToken has been successfully issued via an authorization code.
   *
   * If you don't support Authorization Code Grant, then you don't need to implement this
   * method.
   *
   * @param code Client-sent authorization code
   */
  def deleteAuthCode(code: String): Future[Unit]

  /**
   * Find authorized information by refresh token.
   *
   * If you don't support Refresh Token Grant then doesn't need implementing.
   *
   * @param refreshToken Client sends refresh token which is created by system.
   * @return Return authorized information that matched the refresh token.
   */
  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[U]]]

}
