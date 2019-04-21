package scalaoauth2.provider

import java.util.Date

import scala.concurrent.Future

class MockDataHandler extends DataHandler[User] {

  override def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = Future.successful(false)

  override def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = Future.successful(None)

  override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = Future.successful(None)

  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = Future.successful(None)

  override def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(None)

  override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = Future.successful(None)

  override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = Future.successful(None)

  override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = Future.successful(AccessToken("", Some(""), Some(""), Some(0L), new Date()))

  override def deleteAuthCode(code: String): Future[Unit] = Future.successful(())
}

trait User {
  def id: Long
  def name: String
}

case class MockUser(id: Long, name: String) extends User
