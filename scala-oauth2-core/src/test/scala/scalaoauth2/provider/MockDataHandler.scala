package scalaoauth2.provider

import java.util.Date

import scala.concurrent.Future

trait User {
  def id: Long
}

case class MockUser(id: Long, name: String) extends User

class MockDataHandler extends DataHandler[User] {

  def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = Future.successful(false)

  def findUser(username: String, password: String): Future[Option[User]] = Future.successful(None)

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = Future.successful(None)

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = Future.successful(None)

  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Future[Option[User]] = Future.successful(None)

  def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(None)

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = Future.successful(None)
  
  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = Future.successful(None)

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

}
