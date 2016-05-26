# oauth2-server for Scala [![Build Status](https://travis-ci.org/nulab/scala-oauth2-provider.svg?branch=master)](https://travis-ci.org/nulab/scala-oauth2-provider)

[The OAuth 2.0](http://tools.ietf.org/html/rfc6749) server-side implementation written in Scala.

This provides OAuth 2.0 server-side functionality and supporting function for [Play Framework](http://www.playframework.com/). Play Framework 2.2, 2.3, 2.4 and 2.5 are now supported.

The idea of this library originally comes from [oauth2-server](https://github.com/yoichiro/oauth2-server) which is Java implementation of OAuth 2.0.

## Supported OAuth features

This library supports all grant types.

- Authorization Code Grant
- Resource Owner Password Credentials Grant
- Client Credentials Grant
- Implicit Grant

and an access token type called [Bearer](http://tools.ietf.org/html/rfc6750).

## Setup

If you'd like to use this with Play Framework, add "play2-oauth2-provider" to library dependencies of your project.

### For Play Framework 2.5

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.17.2"
)
```

### For Play Framework 2.4

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.16.1"
)
```

### For Play Framework 2.3

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.14.0"
)
```

### For Play Framework 2.2

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.7.4"
)
```

### Other frameworks

Add "scala-oauth2-core" instead. In this case, you need to implement your own OAuth provider working with web framework you use.

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "scala-oauth2-core" % "0.17.2"
)
```

## How to use

### Implement DataHandler

Whether you use Play Framework or not, you have to implement ```DataHandler``` trait and make it work with your own ```User``` class that may be already defined in your application.

```scala
case class User(id: Long, name: String, hashedPassword: String)

class MyDataHandler extends DataHandler[User] {

  def validateClient(request: AuthorizationRequest): Future[Boolean] = ???

  def findUser(request: AuthorizationRequest): Future[Option[User]] = ???

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
  redirectUri: Option[String]
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

### Work with Play Framework

You should follow four steps below to work with Play Framework.

* Customizing Grant Handlers
* Define a controller to issue access token
* Assign a route to the controller
* Access to an authorized resource

You want to use which grant types are supported or to use a customized handler for a grant type, you should override the ```handlers``` map in a customized ```TokenEndpoint``` trait.

```scala
class MyTokenEndpoint extends TokenEndpoint {
  override val handlers = Map(
    OAuthGrantType.AUTHORIZATION_CODE -> new AuthorizationCode(),
    OAuthGrantType.REFRESH_TOKEN -> new RefreshToken(),
    OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials(),
    OAuthGrantType.PASSWORD -> new Password(),
    OAuthGrantType.IMPLICIT -> new Implicit()
  )
}
```

Here's an example of a customized ```TokenEndpoint``` that 1) only supports the ```password``` grant type, and 2) customizes the ```password``` grant type handler to not require client credentials:

```scala
class MyTokenEndpoint extends TokenEndpoint {
  val passwordNoCred = new Password() {
    override def clientCredentialRequired = false
  }

  override val handlers = Map(
    OAuthGrantType.PASSWORD -> passwordNoCred
  )
}
```

Define your own controller with mixining ```OAuth2Provider``` trait provided by this library to issue access token with customized `TokenEndpoint`.

```scala
import scalaoauth2.provider._
object OAuth2Controller extends Controller with OAuth2Provider {
  override val tokenEndpoint = new MyTokenEndpoint()

  def accessToken = Action.async { implicit request =>
    issueAccessToken(new MyDataHandler())
  }
}
```

Then, assign a route to the controller that OAuth clients will access to.

```
POST    /oauth2/access_token                    controllers.OAuth2Controller.accessToken
```

Finally, you can access to an authorized resource like this:

```scala
import scalaoauth2.provider._
object MyController extends Controller with OAuth2Provider {
  def list = Action.async { implicit request =>
    authorize(new MyDataHandler()) { authInfo =>
      val user = authInfo.user // User is defined on your system
      // access resource for the user
    }
  }
}
```

If you'd like to change the OAuth workflow, modify handleRequest methods of `TokenEndPoint` and `ProtectedResource` traits.

### Using Action composition

You can write more easily authorize action by using Action composition.

Play Framework's documentation is [here](https://www.playframework.com/documentation/2.5.x/ScalaActionsComposition).

```scala
object MyController extends Controller {

  import scalaoauth2.provider.OAuth2ProviderActionBuilders._

  def list = AuthorizedAction(new MyDataHandler()) { request =>
    val user = request.authInfo.user // User is defined on your system
    // access resource for the user
  }
}
```

## Examples

### Play Framework 2.5

- https://github.com/tsuyoshizawa/scala-oauth2-provider-example-skinny-orm

### Play Framework 2.3

- https://github.com/davidseth/scala-oauth2-provider-slick

### Play Framework 2.2

- https://github.com/oyediyildiz/scala-oauth2-provider-example
- https://github.com/tuxdna/play-oauth2-server

## Application using this library

- [Typetalk](https://typetalk.in/)
- [Backlog](https://backlogtool.com/)
- [Flic by Shortcut Labs](
)
