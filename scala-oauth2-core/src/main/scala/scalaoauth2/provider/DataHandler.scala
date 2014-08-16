package scalaoauth2.provider

import java.util.Date
import scala.concurrent.Future

case class AccessTokenRequest[U](clientId: String, clientSecret: String, user: U)

/**
 * Access token
 *
 * @param token Access token is used to authentication.
 * @param refreshToken Refresh token is used to re-issue access token.
 * @param scope Inform the client of the scope of the access token issued.
 * @param expiresIn Expiration date of access token. Unit is seconds.
 * @param createdAt Access token is created date.
 */
case class AccessToken(token: String, refreshToken: Option[String], scope: Option[String], expiresIn: Option[Long], createdAt: Date)

/**
 * Authorized information
 *
 * @param user Authorized user which is registered on system.
 * @param clientId Using client id which is registered on system.
 * @param scope Inform the client of the scope of the access token issued.
 * @param redirectUri This value is used by Authorization Code Grant.
 */
case class AuthInfo[U](user: U, clientId: String, scope: Option[String], redirectUri: Option[String])

/**
 * Provide accessing to data storage for using OAuth 2.0.
 *
 * <h3>[Authorization phases]</h3>
 * 
 * <h4>Authorization Code Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findAuthInfoByCode(code)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>isAccessTokenExpired(token)</li>
 *   <li>refreshAccessToken(authInfo, token)
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Refresh Token Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findAuthInfoByRefreshToken(refreshToken)</li>
 *   <li>refreshAccessToken(authInfo, refreshToken)</li>
 * </ul>
 * 
 * <h4>Resource Owner Password Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findUser(username, password)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>isAccessTokenExpired(token)</li>
 *   <li>refreshAccessToken(authInfo, token)
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Client Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findClientUser(clientId, clientSecret)</li>
 *   <li>getStoredAccessToken(authInfo)</li>
 *   <li>isAccessTokenExpired(token)</li>
 *   <li>refreshAccessToken(authInfo, token)
 *   <li>createAccessToken(authInfo)</li>
 * </ul>
 *   
 * <h3>[Access to Protected Resource phase]</h3>
 * <ul>
 *   <li>findAccessToken(token)</li>
 *   <li>isAccessTokenExpired(token)</li>
 *   <li>findAuthInfoByAccessToken(token)</li>
 * </ul>
 */
trait DataHandler[U] {

  /**
   * Verify proper client with parameters for issue an access token.
   *
   * @param clientId Client send this value which is registered by application.
   * @param clientSecret Client send this value which is registered by application.
   * @param grantType Client send this value which is registered by application.
   * @return true if request is a regular client, false if request is a illegal client.
   */
  def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean]

  /**
   * Find userId with username and password these are used on your system.
   * If you don't support Resource Owner Password Credentials Grant then doesn't need implementing.
   *
   * @param username Client send this value which is used on your system.
   * @param password Client send this value which is used on your system.
   * @return Including UserId to Option if could find the user, None if couldn't find.
   */
  def findUser(username: String, password: String): Future[Option[U]]

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
   * @param code Client send authorization code which is registered by system.
   * @return Return authorized information that matched the code.
   */
  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[U]]]

  /**
   * Find authorized information by refresh token.
   *
   * If you don't support Refresh Token Grant then doesn't need implementing.
   *
   * @param refreshToken Client send refresh token which is created by system.
   * @return Return authorized information that matched the refresh token.
   */
  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[U]]]

  /**
   * Find user by clientId and clientSecret.
   *
   * If you don't support Client Credentials Grant then doesn't need implementing.
   *
   * @param clientId Client send this value which is registered by application.
   * @param clientSecret Client send this value which is registered by application.
   * @return Return user that matched both values.
   */
  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Future[Option[U]]

  /**
   * Find AccessToken object by access token code.
   *
   * @param token Client send access token which is created by system.
   * @return Return access token that matched the token.
   */
  def findAccessToken(token: String): Future[Option[AccessToken]]

  /**
   * Find authorized information by access token.
   *
   * @param accessToken This value is AccessToken.
   * @return Return authorized information if the parameter is available.
   */
  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[U]]]

  /**
   * Check expiration.
   * 
   * @param accessToken accessToken
   * @return true if accessToken expired
   */
  def isAccessTokenExpired(accessToken: AccessToken): Boolean = {
    accessToken.expiresIn.map { expiresIn =>
      val now = System.currentTimeMillis()
      accessToken.createdAt.getTime + expiresIn * 1000 <= now
    }.getOrElse(false)
  }

}
