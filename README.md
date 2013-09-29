# oauth2-server for Scala

This library is able to be possible to provide OAuth2 server on your service by using Scala. ([Specification](http://tools.ietf.org/html/rfc6749))

## Current supported Grant types

- Authorization Code Grant
- Resource Owner Password Credentials Grant
- Client Credentials Grant

## Current supported token types

Bearer ([http://tools.ietf.org/html/rfc6750](http://tools.ietf.org/html/rfc6750))

## Setup

### SBT

Playframework 2.1 and 2.2 is supporting.

```
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "play2-oauth2-provider" % "0.1.0"
)
```

If you want to use only Scala project or try making provider with another front-end library.

```
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "scala-oauth2-core" % "0.1.0"
)
```

### Implement DataHandler

You have to implement DataHandler trait which isn't depended specific data storage.

## With Playframework

### Defining Controller for register access token on your system

```
import scalaoauth2.provider._
object OAuth2Controller extends Controller with OAuth2Provider {
  def accessToken = Action { implicit request =>
    issueAccessToken(new MyDataHandler())
  }
}
```

### routes

To provide a route for user will be able to register access token.

```
POST    /oauth2/access_token                    controllers.OAuth2Controller.accessToken
```

### Access to resource
```
import scalaoauth2.provider._
object BookController extends Controller with OAuthProvider {
  def list = Action { implicit request =>
    authorize(new MyDataHandler()) { authInfo =>
      val user = User.findById(authInfo.userId) // User is defined on your system
      // access resource for the user
    }
  }
}
```

If you want to handle the oauth flow more customizable, it would resolve using Token/ProtectedResource#handleRequest directly.

This library has been based from [https://github.com/yoichiro/oauth2-server](https://github.com/yoichiro/oauth2-server) for Scala.
