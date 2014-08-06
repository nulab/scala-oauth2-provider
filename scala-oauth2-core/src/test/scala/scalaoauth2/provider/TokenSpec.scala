package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class TokenSpec extends FlatSpec with ScalaFutures {

  def successfulDataHandler() = new MockDataHandler() {

    override def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = Future.successful(true)

    override def findUser(username: String, password: String): Future[Option[MockUser]] = Future.successful(Some(MockUser(10000, "username")))

    override def createAccessToken(authInfo: AuthInfo[MockUser]): Future[AccessToken] = Future.successful(AccessToken("token1", None, Some("all"), Some(3600), new java.util.Date()))

  }

  it should "be handled request" in {
    val request = AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    val futureResult = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(futureResult) { result => result should be ('right)}
  }

  it should "be error if grant type doesn't exist" in {
    val request = AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    val futureResult = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(futureResult) { result =>
      intercept[InvalidRequest] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
    }
  }

  it should "be error if grant type is wrong" in {
    val request = AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
      Map("grant_type" -> Seq("test"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    val futureResult = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(futureResult) { result =>
      intercept[UnsupportedGrantType] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
    }
  }

  it should "be invalid request without client credential" in {
    val request = AuthorizationRequest(
      Map(),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulDataHandler()
    val futureResult = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(futureResult) {result =>
      intercept[InvalidRequest] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
    }
  }

  it should "be invalid client if client information is wrong" in {
    val request = AuthorizationRequest(
      Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
      Map("grant_type" -> Seq("password"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = new MockDataHandler() {
      override def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = Future.successful(false)

    }

    val futureResult = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(futureResult) { result =>
      intercept[InvalidClient] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
    }
  }
}
