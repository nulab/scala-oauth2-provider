package scalaoauth2.provider

import java.util.Date

import scala.util.{Failure, Success, Try}

/** Provide accessing to data storage for using OAuth 2.0.
  */
trait DataHandler[U]
    extends AuthorizationHandler[U]
    with ProtectedResourceHandler[U]

/** Access token
  *
  * @param token
  *   Access token is used to authentication.
  * @param refreshToken
  *   Refresh token is used to re-issue access token.
  * @param scope
  *   Inform the client of the scope of the access token issued.
  * @param lifeSeconds
  *   Life of the access token since its creation. In seconds.
  * @param createdAt
  *   Access token is created date.
  * @param params
  *   Additional parameters to add information/restriction on given Access
  *   token.
  */
case class AccessToken(
    token: String,
    refreshToken: Option[String],
    scope: Option[String],
    lifeSeconds: Option[Long],
    createdAt: Date,
    params: Map[String, String] = Map.empty[String, String]
) {
  def isExpired: Boolean = expiresIn.exists(_ < 0)

  val expiresIn: Option[Long] = expirationTimeInMilis map { expTime =>
    (expTime - System.currentTimeMillis) / 1000
  }

  private def expirationTimeInMilis: Option[Long] = lifeSeconds map {
    lifeSecs =>
      createdAt.getTime + lifeSecs * 1000
  }
}

sealed trait CodeChallengeMethod
case object Plain extends CodeChallengeMethod {
  override def toString: String = "plain"
}
case object S256 extends CodeChallengeMethod {
  override def toString: String = "S256"
}

object CodeChallengeMethod {
  def apply(value: String): Try[CodeChallengeMethod] = {
    value match {
      case "S256"  => Success(S256)
      case "plain" => Success(Plain)
      case _ => Failure(new InvalidRequest("transform algorithm not supported"))
    }
  }
}

/** Authorized information
  *
  * @param user
  *   Authorized user which is registered on system.
  * @param clientId
  *   Using client id which is registered on system.
  * @param scope
  *   Inform the client of the scope of the access token issued.
  * @param redirectUri
  *   This value is used by Authorization Code Grant.
  * @param codeChallenge
  *   This value is used by Authorization Code Grant for PKCE support.
  * @param codeChallengeMethod
  *   This value is used by Authorization Code Grant for PKCE support.
  */
case class AuthInfo[+U](
    user: U,
    clientId: Option[String],
    scope: Option[String],
    redirectUri: Option[String],
    codeChallenge: Option[String] = None,
    codeChallengeMethod: Option[CodeChallengeMethod] = None
)
