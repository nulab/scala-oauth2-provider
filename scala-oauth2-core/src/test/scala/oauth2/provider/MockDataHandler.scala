package scalaoauth2.provider

import java.util.Date

case class MockUser(id: Long, name: String)

class MockDataHandler extends DataHandler[MockUser] {

  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean  = false

  def findUser(username: String, password: String): Option[MockUser] = None

  def createOrUpdateAuthInfo(user: MockUser, clientId: String, scope: Option[String]): Option[AuthInfo[MockUser]] = None

  def createOrUpdateAccessToken(authInfo: AuthInfo[MockUser]): AccessToken = AccessToken("", "", 0L, new Date())

  def findAuthInfoByCode(code: String): Option[AuthInfo[MockUser]] = None

  def findAuthInfoByRefreshToken(refreshToken: String): Option[AuthInfo[MockUser]] = None

  def findClientUser(clientId: String, clientSecret: String): Option[MockUser] = None

  def findAccessToken(token: String): Option[AccessToken] = None

  def findAuthInfoById(authId: String): Option[AuthInfo[MockUser]] = None

}
