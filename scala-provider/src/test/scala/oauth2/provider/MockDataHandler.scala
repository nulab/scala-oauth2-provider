package scalaoauth2.provider

import java.util.Date

class MockDataHandler extends DataHandler {

  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean  = false

  def findUserId(username: String, password: String): Option[String] = None

  def createOrUpdateAuthInfo(userId: String, clientId: String, scope: Option[String]): Option[AuthInfo] = None

  def createOrUpdateAccessToken(authInfo: AuthInfo): AccessToken = AccessToken("", "", 0L, new Date())

  def findAuthInfoByCode(code: String): Option[AuthInfo] = None

  def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo] = None

  def findClientUserId(clientId: String, clientSecret: String): Option[String] = None

  def findAccessToken(token: String): Option[AccessToken] = None

  def findAuthInfoById(authId: String): Option[AuthInfo] = None

}
