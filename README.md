# oauth2-server for Scala [![Build Status](https://travis-ci.org/nulab/scala-oauth2-provider.svg?branch=master)](https://travis-ci.org/nulab/scala-oauth2-provider)

[The OAuth 2.0](http://tools.ietf.org/html/rfc6749) server-side implementation written in Scala.

This provides OAuth 2.0 server-side functionality and supporting function for [Play Framework](http://www.playframework.com/) and [Akka HTTP](http://akka.io/).

The idea of this library originally comes from [oauth2-server](https://github.com/yoichiro/oauth2-server) which is Java implementation of OAuth 2.0.

## Supported OAuth features

This library supports all grant types.

- Authorization Code Grant (PKCE Authorization Code Grants are supported)
- Resource Owner Password Credentials Grant
- Client Credentials Grant
- Implicit Grant

and an access token type called [Bearer](http://tools.ietf.org/html/rfc6750).

## Setup

### Play Framework

See [the project](https://github.com/nulab/play2-oauth2-provider)

### Akka HTTP

See [the project](https://github.com/nulab/akka-http-oauth2-provider)

### Other frameworks

Add `scala-oauth2-core` library dependencies of your project.
In this case, you need to implement your own OAuth provider working with web framework you use.

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "scala-oauth2-core" % "1.4.0"
)
```

## How to use

### Implement DataHandler

Whether you use Play Framework or not, you have to implement ```DataHandler``` trait and make it work with your own ```User``` class that may be already defined in your application.

```scala
case class User(id: Long, name: String, hashedPassword: String)

class MyDataHandler extends DataHandler[User] {

  def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = ???

  def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = ???

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = ???

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = ???

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = ???

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = ???

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = ???

  def deleteAuthCode(code: String): Future[Unit] = ???

  def findAccessToken(token: String): Future[Option[AccessToken]] = ???

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = ???

}
```

If your data access is blocking for the data storage, then you just wrap your implementation in the ```DataHandler``` trait with ```Future.successful(...)```.

For more details, refer to Scaladoc of ```DataHandler```.

### AuthInfo

```DataHandler``` returns ```AuthInfo``` as authorized information.
```AuthInfo``` is made up of the following fields.

```scala
case class AuthInfo[User](
  user: User,
  clientId: Option[String],
  scope: Option[String],
  redirectUri: Option[String],
  codeChallenge: Option[String] = None,
  codeChallengeMethod: Option[CodeChallengeMethod] = None
)
```

- user
  - ```user``` is authorized by DataHandler
- clientId
  - ```clientId``` which is sent from a client has been verified by ```DataHandler```
  - If your application requires client_id for client authentication, you can get ```clientId``` as below
    - ```val clientId = authInfo.clientId.getOrElse(throw new InvalidClient())```
- scope
  - inform the client of the scope of the access token issued
- redirectUri
  - This value must be enabled on authorization code grant
- codeChallenge:
  - This value is OPTIONAL. Only set this value if doing a PKCE authorization request. When set, PKCE rules apply on the AuthorizationCode Grant Handler
  - This value is from a PKCE authorization request. This is the challenge supplied during the auth request if given.
- codeChallengeMethod:
  - This value is OPTIONAL and used only by PKCE when a codeChallenge value is also set.
  - This value is from a PKCE authorization request. This is the method used to transform the code verifier. Must be either Plain or S256. If not specified and codeChallenge is provided then Plain is assumed (per RFC7636)
