package scalaoauth2.provider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class GrantHandlerResult(tokenType: String, accessToken: String, expiresIn: Option[Long], refreshToken: Option[String], scope: Option[String])

trait GrantHandler {

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult]

  /**
   * Returns valid access token.
   *
   * @param dataHandler
   * @param authInfo
   * @return
   */
  def issueAccessToken[U](dataHandler: DataHandler[U], authInfo: AuthInfo[U]): Future[GrantHandlerResult] = {
    dataHandler.getStoredAccessToken(authInfo).flatMap { optionalAccessToken =>
      (optionalAccessToken match {
        case Some(token) if dataHandler.isAccessTokenExpired(token) => {
          token.refreshToken.map(dataHandler.refreshAccessToken(authInfo, _)).getOrElse(dataHandler.createAccessToken(authInfo))
        }
        case Some(token) => Future.successful(token)
        case None => dataHandler.createAccessToken(authInfo)
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

class RefreshToken(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("Authorization header is invalid"))
    val refreshToken = request.requireRefreshToken

    dataHandler.findAuthInfoByRefreshToken(refreshToken).flatMap { authInfoOption =>
      val authInfo = authInfoOption.getOrElse(throw new InvalidGrant("Authorized information is not found by the refresh token"))
      if (authInfo.clientId != clientCredential.clientId) {
        throw new InvalidClient
      }

      dataHandler.refreshAccessToken(authInfo, refreshToken).map { accessToken =>
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

class Password(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("Authorization header is invalid"))
    val username = request.requireUsername
    val password = request.requirePassword

    dataHandler.findUser(username, password).flatMap { userOption =>
      val user = userOption.getOrElse(throw new InvalidGrant("username or password is incorrect"))
      val scope = request.scope
      val clientId = clientCredential.clientId
      val authInfo = AuthInfo(user, clientId, scope, None)

      issueAccessToken(dataHandler, authInfo)
    }
  }
}

class ClientCredentials(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("Authorization header is invalid"))
    val clientSecret = clientCredential.clientSecret
    val clientId = clientCredential.clientId
    val scope = request.scope

    dataHandler.findClientUser(clientId, clientSecret, scope).flatMap { userOption =>
      val user = userOption.getOrElse(throw new InvalidGrant("client_id or client_secret or scope is incorrect"))
      val authInfo = AuthInfo(user, clientId, scope, None)

      issueAccessToken(dataHandler, authInfo)
    }
  }

}

class AuthorizationCode(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("Authorization header is invalid"))
    val clientId = clientCredential.clientId
    val code = request.requireCode
    val redirectUri = request.redirectUri

    dataHandler.findAuthInfoByCode(code).flatMap { authInfoOption =>
      val authInfo = authInfoOption.getOrElse(throw new InvalidGrant("Authorized information is not found by the code"))
      if (authInfo.clientId != clientId) {
        throw new InvalidClient
      }

      if (authInfo.redirectUri.isDefined && authInfo.redirectUri != redirectUri) {
        throw new RedirectUriMismatch
      }

      issueAccessToken(dataHandler, authInfo)
    }
  }

}
