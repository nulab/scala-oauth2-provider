package scalaoauth2.provider

import java.util.Date

/**
 * Provide accessing to data storage for using OAuth 2.0.
 */
trait DataHandler[U] extends AuthorizationHandler[U] with ProtectedResourceHandler[U]

/**
 * Access token
 *
 * @param token Access token is used to authentication.
 * @param refreshToken Refresh token is used to re-issue access token.
 * @param scope Inform the client of the scope of the access token issued.
 * @param lifeSeconds Life of the access token since its creation. In seconds.
 * @param createdAt Access token is created date.
 */
case class AccessToken(token: String, refreshToken: Option[String], scope: Option[String], lifeSeconds: Option[Long], createdAt: Date) {
  def isExpired: Boolean = expirationTimeInMilis.exists { expTime =>
    expTime <= System.currentTimeMillis
  }

  def expiresIn: Option[Long] = expirationTimeInMilis map { expTime =>
    (expTime - System.currentTimeMillis) / 1000
  }

  private def expirationTimeInMilis: Option[Long] = lifeSeconds map { lifeSecs =>
    createdAt.getTime + lifeSecs * 1000
  }
}

/**
 * Authorized information
 *
 * @param user Authorized user which is registered on system.
 * @param clientId Using client id which is registered on system.
 * @param scope Inform the client of the scope of the access token issued.
 * @param redirectUri This value is used by Authorization Code Grant.
 */
case class AuthInfo[+U](user: U, clientId: Option[String], scope: Option[String], redirectUri: Option[String])
