package scalaoauth2.provider


case class GrantHandlerResult(tokenType: String, accessToken: String, expiresIn: Option[Long], refreshToken: Option[String], scope: Option[String])

trait GrantHandler {

  def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): GrantHandlerResult


  /**
   * Returns valid access token.
   * 
   * @param dataHandler
   * @param authInfo
   * @return 
   */
  def issueAccessToken[U](dataHandler: DataHandler[U], authInfo: AuthInfo[U]): GrantHandlerResult = {
    val accessToken = dataHandler.getStoredAccessToken(authInfo) match {
      case Some(token) if dataHandler.isAccessTokenExpired(token) =>
        token.refreshToken.map(dataHandler.refreshAccessToken(authInfo, _)).getOrElse(dataHandler.createAccessToken(authInfo))
      case Some(token) => token
      case None => dataHandler.createAccessToken(authInfo)
    }
    
    GrantHandlerResult(
      "Bearer",
      accessToken.token,
      accessToken.expiresIn,
      accessToken.refreshToken,
      accessToken.scope
    )
  }
}

class RefreshToken(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): GrantHandlerResult = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val refreshToken = request.requireRefreshToken
    val authInfo = dataHandler.findAuthInfoByRefreshToken(refreshToken).getOrElse(throw new InvalidGrant("NotFound"))
    if (authInfo.clientId != clientCredential.clientId) {
      throw new InvalidClient
    }
    
    val accessToken = dataHandler.refreshAccessToken(authInfo, refreshToken)
    GrantHandlerResult(
      "Bearer",
      accessToken.token,
      accessToken.expiresIn,
      accessToken.refreshToken,
      accessToken.scope
    )
  }
}

class Password(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): GrantHandlerResult = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val username = request.requireUsername
    val password = request.requirePassword
    val user = dataHandler.findUser(username, password).getOrElse(throw new InvalidGrant())
    val scope = request.scope
    val clientId = clientCredential.clientId
    val authInfo = AuthInfo(user, clientId, scope, None)

    issueAccessToken(dataHandler, authInfo)
  }
}

class ClientCredentials(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): GrantHandlerResult = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val clientSecret = clientCredential.clientSecret
    val clientId = clientCredential.clientId
    val scope = request.scope
    val user = dataHandler.findClientUser(clientId, clientSecret, scope).getOrElse(throw new InvalidGrant())
    val authInfo = AuthInfo(user, clientId, scope, None)
    
    issueAccessToken(dataHandler, authInfo)
  }

}

class AuthorizationCode(clientCredentialFetcher: ClientCredentialFetcher) extends GrantHandler {

  override def handleRequest[U](request: AuthorizationRequest, dataHandler: DataHandler[U]): GrantHandlerResult = {
    val clientCredential = clientCredentialFetcher.fetch(request).getOrElse(throw new InvalidRequest("BadRequest"))
    val clientId = clientCredential.clientId
    val code = request.requireCode
    val redirectUri = request.redirectUri
    val authInfo = dataHandler.findAuthInfoByCode(code).getOrElse(throw new InvalidGrant())
    if (authInfo.clientId != clientId) {
      throw new InvalidClient
    }

    if (authInfo.redirectUri.isDefined && authInfo.redirectUri != redirectUri) {
      throw new RedirectUriMismatch
    }

    issueAccessToken(dataHandler, authInfo)
  }

}
