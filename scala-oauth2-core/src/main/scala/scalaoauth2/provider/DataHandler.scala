package scalaoauth2.provider

import java.util.Date

/**
 * Access token
 *
 * @param authId This value is consistent with AuthInfo has the id.
 * @param token Access token is used to authentication.
 * @param expiresIn Expiration date of access token. Unit is seconds.
 * @param createdAt Access token is created date.
 */
case class AccessToken(authId: String, token: String, expiresIn: Long, createdAt: Date)

/**
 * Authorized information
 *
 * @param id Primary key.
 * @param userId Authorized user id which is registered on system.
 * @param clientId Using client id which is registered on system.
 * @param refreshToken This value is used by Refresh Token Grant.
 * @param scope This value is used by permit to API.
 * @param code This value is used by Authorization Code Grant.
 * @param redirectUri This value is used by Authorization Code Grant.
 */
case class AuthInfo(id: String, userId: String, clientId: String, refreshToken: Option[String], scope: Option[String], code: Option[String], redirectUri: Option[String])

/**
 * Provide accessing to data storage for using OAuth 2.0.
 *
 * <h3>[Authorization phases]</h3>
 * 
 * <h4>Authorization Code Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findAuthInfoByCode(code)</li>
 *   <li>createOrUpdateAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Refresh Token Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findAuthInfoByRefreshToken(refreshToken)</li>
 *   <li>createOrUpdateAuthInfo</li>
 *   <li>createOrUpdateAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Resource Owner Password Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findUserId(username, password)</li>
 *   <li>createOrUpdateAuthInfo(clientId, userId, scope)</li>
 *   <li>createOrUpdateAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Client Credentials Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findClientUserId(clientId, clientSecret)</li>
 *   <li>createOrUpdateAuthInfo(clientId, userId, scope)</li>
 *   <li>createOrUpdateAccessToken(authInfo)</li>
 * </ul>
 *   
 * <h3>[Access to Protected Resource phase]</h3>
 * <ul>
 *   <li>findAccessToken(token)</li>
 *   <li>findAuthInfoById(authId)</li>
 * </ul>
 */
trait DataHandler {

  /**
   * Verify proper client with parameters for issue an access token.
   *
   * @param clientId Client send this value which is registered by application.
   * @param clientSecret Client send this value which is registered by application.
   * @param grantType Client send this value which is registered by application.
   * @return true if request is a regular client, false if request is a illegal client.
   */
  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean

  /**
   * Find userId with username and password these are used on your system.
   * If you don't support Resource Owner Password Credentials Grant then doesn't need implementing.
   *
   * @param username Client send this value which is used on your system.
   * @param password Client send this value which is used on your system.
   * @return Including UserId to Option if could find the user, None if couldn't find.
   */
  def findUserId(username: String, password: String): Option[String]

  /**
   * Create or update authorized information by userId, clientId and scope.
   *
   * Returning None when create access token then response will be invalid grant.
   * This method also is called at the time of refresh token, it can safely return None.
   *
   * @param userId DataHandler got this value by authentication.
   * @param clientId Client send this value which is registered by application.
   * @param scope Client request scope value. If client doesn't request then it will be None.
   * @return created or updated authorize information. Return None if you want to strictly verify the scope.
   */
  def createOrUpdateAuthInfo(userId: String, clientId: String, scope: Option[String]): Option[AuthInfo]

  /**
   * Create or update access token by authorized information.
   *
   * @param authInfo This value is created by createOrUpdateAuthInfo method.
   * @return Access token return to client.
   */
  def createOrUpdateAccessToken(authInfo: AuthInfo): AccessToken

  /**
   * Find authorized information by authorization code.
   *
   * If you don't support Authorization Code Grant then doesn't need implementing.
   *
   * @param code Client send authorization code which is registered by system.
   * @return Return authorized information that matched the code.
   */
  def findAuthInfoByCode(code: String): Option[AuthInfo]

  /**
   * Find authorized information by refresh token.
   *
   * If you don't support Refresh Token Grant then doesn't need implementing.
   *
   * @param refreshToken Client send refresh token which is created by system.
   * @return Return authorized information that matched the refresh token.
   */
  def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo]

  /**
   * Find userId by clientId and clientSecret.
   *
   * If you don't support Client Credentials Grant then doesn't need implementing.
   *
   * @param clientId Client send this value which is registered by application.
   * @param clientSecret Client send this value which is registered by application.
   * @return Return userId that matched both values.
   */
  def findClientUserId(clientId: String, clientSecret: String): Option[String]

  /**
   * Find AccessToken object by access token code.
   *
   * @param token Client send access token which is created by system.
   * @return Return access token that matched the token.
   */
  def findAccessToken(token: String): Option[AccessToken]

  /**
   * Find authorized information by the primary key.
   * The value is associated with AccessToken.
   *
   * @param authId This value is passed from AccessToken.
   * @return Return authorized information if the parameter is available.
   */
  def findAuthInfoById(authId: String): Option[AuthInfo]

}
