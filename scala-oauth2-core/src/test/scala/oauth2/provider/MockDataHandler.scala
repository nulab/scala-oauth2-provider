package scalaoauth2.provider

import java.util.Date

case class MockUser(id: Long, name: String)

class MockDataHandler extends DataHandler[MockUser] {

  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean  = false

  def findUser(username: String, password: String): Option[MockUser] = None

  def createAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("", Some(""), Some(""), Some(0L), new Date())

  def findAuthInfoByCode(code: String): Option[AuthInfo[MockUser]] = None

  def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo[MockUser]] = None

  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Option[MockUser] = None

  def findAccessToken(token: String): Option[AccessToken] = None

  def findAuthInfoByAccessToken(accessToken: AccessToken): Option[AuthInfo[MockUser]] = None
  
  def getStoredAccessToken(authInfo: AuthInfo[MockUser]): Option[AccessToken] = None

  def refreshAccessToken(authInfo: AuthInfo[MockUser], refreshToken: String): AccessToken = AccessToken("", Some(""), Some(""), Some(0L), new Date())

}
