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

    val accessTokenFuture: Future[Option[AccessToken]] = dataHandler.getStoredAccessToken(authInfo)

    accessTokenFuture.flatMap { optionalAccessToken =>

      val accessToken: Future[AccessToken] = optionalAccessToken match {
        case Some(token) if dataHandler.isAccessTokenExpired(token) => {
          token.refreshToken.map(dataHandler.refreshAccessToken(authInfo, _)).getOrElse(dataHandler.createAccessToken(authInfo))
        }
        case Some(token) => Future.successful(token)
        case None => dataHandler.createAccessToken(authInfo)
      }

      accessToken.map { token =>
        GrantHandlerResult(
          "Bearer",
          token.token,
          token.expiresIn,
          token.refreshToken,
          token.scope
        )
      }
    }
  }
}

class RefreshToken(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {

    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val refreshToken = request.requireRefreshToken

    val authInfoFuture: Future[Option[AuthInfo[U]]] = dataHandler.findAuthInfoByRefreshToken(refreshToken)

    authInfoFuture.flatMap { authInfoOption =>
      val authInfo = authInfoOption.getOrElse(throw new InvalidGrant("NotFound"))
      if (authInfo.clientId != clientCredential.clientId) {
        throw new InvalidClient
      }

      val accessToken: Future[AccessToken] = dataHandler.refreshAccessToken(authInfo, refreshToken)
      accessToken.map { token =>
        GrantHandlerResult(
          "Bearer",
          token.token,
          token.expiresIn,
          token.refreshToken,
          token.scope
        )
      }
    }
  }
}

class Password(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val username = request.requireUsername
    val password = request.requirePassword

    val userFuture: Future[Option[U]] = dataHandler.findUser(username, password)

    userFuture.flatMap { userOption =>
      val user = userOption.getOrElse(throw new InvalidGrant())
      val scope = request.scope
      val clientId = clientCredential.clientId
      val authInfo = AuthInfo(user, clientId, scope, None)

      issueAccessToken(dataHandler, authInfo)
    }
  }
}

class ClientCredentials(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val clientSecret = clientCredential.clientSecret
    val clientId = clientCredential.clientId
    val scope = request.scope

    val userFuture: Future[Option[U]] = dataHandler.findClientUser(clientId, clientSecret, scope)

    userFuture.flatMap { userOption =>
      val user = userOption.getOrElse(throw new InvalidGrant())
      val authInfo = AuthInfo(user, clientId, scope, None)

      issueAccessToken(dataHandler, authInfo)
    }
  }

}

class AuthorizationCode(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): Future[GrantHandlerResult] = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val clientId = clientCredential.clientId
    val code = request.requireCode
    val redirectUri = request.redirectUri

    val authInfoFuture: Future[Option[AuthInfo[U]]] = dataHandler.findAuthInfoByCode(code)

    authInfoFuture.flatMap { authInfoOption =>
      val authInfo = authInfoOption.getOrElse(throw new InvalidGrant())

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
