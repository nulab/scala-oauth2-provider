package scalaoauth2.provider

import java.util.Date

case class AccessToken(authId: String, token: String, expiresIn: Long, createdAt: Date)
case class AuthInfo(id: String, userId: String, clientId: String, refreshToken: Option[String], scope: Option[String], code: Option[String], redirectUri: Option[String])

/**
 * <h3>[Authorization phases]</h3>
 * 
 * <h4>Authorization Code Grant</h4>
 * <ul>
 *   <li>validateClient(clientId, clientSecret, grantType)</li>
 *   <li>findAuthInfoByCode(code)</li>
 *   <li>createOrUpdateAccessToken(authInfo)</li>
 * </ul>
 * 
 * <h4>Refresh Token Grant:</h4>
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

  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean

  def findUserId(username: String, password: String): Option[String]

  def createOrUpdateAuthInfo(userId: String, clientId: String, scope: Option[String]): Option[AuthInfo]

  def createOrUpdateAccessToken(authInfo: AuthInfo): AccessToken

  def findAuthInfoByCode(code: String): Option[AuthInfo]

  def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo]

  def findClientUserId(clientId: String, clientSecret: String): Option[String]

  def findAccessToken(token: String): Option[AccessToken]

  def findAuthInfoById(authId: String): Option[AuthInfo]

}
