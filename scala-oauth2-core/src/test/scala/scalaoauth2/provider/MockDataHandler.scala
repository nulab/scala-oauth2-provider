package scalaoauth2.provider

import java.util.Date

import scala.concurrent.Future

case class MockUser(id: Long, name: String)

class MockDataHandler extends DataHandler[MockUser] {

  def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = Future.successful(false)

  def findUser(username: String, password: String): Future[Option[MockUser]] = Future.successful(None)

  def createAccessToken(authInfo: AuthInfo[MockUser]): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(None)

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[MockUser]]] = Future.successful(None)

  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Future[Option[MockUser]] = Future.successful(None)

  def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(None)

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[MockUser]]] = Future.successful(None)
  
  def getStoredAccessToken(authInfo: AuthInfo[MockUser]): Future[Option[AccessToken]] = Future.successful(None)

  def refreshAccessToken(authInfo: AuthInfo[MockUser], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

}
