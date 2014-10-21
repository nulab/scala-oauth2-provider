package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class GrantHandlerResult(tokenType: String, accessToken: String, expiresIn: Option[Long], refreshToken: Option[String], scope: Option[String])

trait GrantHandler {
  /**
   * Controls whether client credentials are required.  Defaults to true but can be overridden to be false when needed.
   * Per the OAuth2 specification, client credentials are required for all grant types except password, where it is up
   * to the authorization provider whether to make them required or not.
   */
  def clientCredentialRequired = true

  def handleRequest[U](request: AuthorizationRequest, maybeClientCredential: Option[ClientCredential], authorizationHandler: AuthorizationHandler[U]): Future[GrantHandlerResult]

  /**
   * Returns valid access token.
   */
  def issueAccessToken[U](handler: AuthorizationHandler[U], authInfo: AuthInfo[U]): Future[GrantHandlerResult] = {
    handler.getStoredAccessToken(authInfo).flatMap { optionalAccessToken =>
      (optionalAccessToken match {
        case Some(token) if token.isExpired => token.refreshToken.map {
          handler.refreshAccessToken(authInfo, _)
        }.getOrElse {
          handler.createAccessToken(authInfo)
        }
        case Some(token) => Future.successful(token)
        case None => handler.createAccessToken(authInfo)
      }).map { accessToken =>
        GrantHandlerResult(
          "Bearer",
          accessToken.token,
          accessToken.expiresIn,
          accessToken.refreshToken,
          accessToken.scope
        )
      }
    }
  }

}

class RefreshToken extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, maybeClientCredential: Option[ClientCredential], handler: AuthorizationHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = maybeClientCredential.getOrElse(throw new InvalidRequest("Client credential is required"))
    val refreshToken = request.requireRefreshToken

    handler.findAuthInfoByRefreshToken(refreshToken).flatMap { authInfoOption =>
      val authInfo = authInfoOption.getOrElse(throw new InvalidGrant("Authorized information is not found by the refresh token"))
      if (authInfo.clientId != Some(clientCredential.clientId)) {
        throw new InvalidClient
      }

      handler.refreshAccessToken(authInfo, refreshToken).map { accessToken =>
        GrantHandlerResult(
          "Bearer",
          accessToken.token,
          accessToken.expiresIn,
          accessToken.refreshToken,
          accessToken.scope
        )
      }
    }
  }
}

class Password extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, maybeClientCredential: Option[ClientCredential], handler: AuthorizationHandler[U]): Future[GrantHandlerResult] = {
    if (clientCredentialRequired && maybeClientCredential.isEmpty) {
      throw new InvalidRequest("Client credential is required")
    }

    val username = request.requireUsername
    val password = request.requirePassword

    handler.findUser(username, password).flatMap { userOption =>
      val user = userOption.getOrElse(throw new InvalidGrant("username or password is incorrect"))
      val scope = request.scope
      val clientId = maybeClientCredential.map { _.clientId }
      val authInfo = AuthInfo(user, clientId, scope, None)

      issueAccessToken(handler, authInfo)
    }
  }
}

class ClientCredentials extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, maybeClientCredential: Option[ClientCredential], handler: AuthorizationHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = maybeClientCredential.getOrElse(throw new InvalidRequest("Client credential is required"))
    val scope = request.scope

    handler.findClientUser(clientCredential, scope).flatMap { optionalUser =>
      val user = optionalUser.getOrElse(throw new InvalidGrant("client_id or client_secret or scope is incorrect"))
      val authInfo = AuthInfo(user, Some(clientCredential.clientId), scope, None)

      issueAccessToken(handler, authInfo)
    }
  }

}

class AuthorizationCode extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, maybeClientCredential: Option[ClientCredential], handler: AuthorizationHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = maybeClientCredential.getOrElse(throw new InvalidRequest("Client credential is required"))
    val clientId = clientCredential.clientId
    val code = request.requireCode
    val redirectUri = request.redirectUri

    handler.findAuthInfoByCode(code).flatMap { optionalAuthInfo =>
      val authInfo = optionalAuthInfo.getOrElse(throw new InvalidGrant("Authorized information is not found by the code"))
      if (authInfo.clientId != Some(clientId)) {
        throw new InvalidClient
      }

      if (authInfo.redirectUri.isDefined && authInfo.redirectUri != redirectUri) {
        throw new RedirectUriMismatch
      }

      issueAccessToken(handler, authInfo)
    }
  }

}
