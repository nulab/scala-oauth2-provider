package scalaoauth2.provider

import java.util.Date
import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

class TokenEndPointSpec extends FlatSpec with ScalaFutures {

  def successfulDataHandler() = new MockDataHandler() {

    override def validateClient(
        maybeClientCredential: Option[ClientCredential],
        request: AuthorizationRequest
    ): Future[Boolean] = Future.successful(true)

    override def findUser(
        maybeClientCredential: Option[ClientCredential],
        request: AuthorizationRequest
    ): Future[Option[User]] =
      Future.successful(Some(MockUser(10000, "username")))

    override def createAccessToken(
        authInfo: AuthInfo[User]
    ): Future[AccessToken] =
      Future.successful(
        AccessToken("token1", None, Some("all"), Some(3600), new Date())
      )

  }

  it should "be handled request" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = successfulDataHandler()
    val f = new TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      result should be(Symbol("right"))
    }
  }

  it should "be error if grant type doesn't exist" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = successfulDataHandler()
    val f = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[InvalidRequest] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
      e.description should be("required parameter: grant_type")
    }
  }

  it should "be error if grant type is wrong" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("test"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = successfulDataHandler()
    val f = TokenEndpoint.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[UnsupportedGrantType] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
      e.description should be("test is not supported")
    }
  }

  it should "be invalid request without client credential" in {
    val request = new AuthorizationRequest(
      Map(),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = successfulDataHandler()
    val f = new TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[InvalidRequest] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
      e.description should be("Client credential is not found")
    }
  }

  it should "not be invalid request without client credential when not required" in {
    val request = new AuthorizationRequest(
      Map(),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = successfulDataHandler()
    val passwordNoCred = new Password() {
      override def clientCredentialRequired = false
    }

    val f = new TokenEndpoint {
      override val handlers = Map("password" -> passwordNoCred)
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      result should be(Symbol("right"))
    }
  }

  it should "be invalid client if client information is wrong" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = new MockDataHandler() {

      override def validateClient(
          maybeClientCredential: Option[ClientCredential],
          request: AuthorizationRequest
      ): Future[Boolean] = Future.successful(false)

    }

    val f = new TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      intercept[InvalidClient] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
    }
  }

  it should "be Failure when DataHandler throws Exception" in {
    val request = new AuthorizationRequest(
      Map(
        "Authorization" -> Seq(
          "Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    def dataHandler = new MockDataHandler() {

      override def validateClient(
          maybeClientCredential: Option[ClientCredential],
          request: AuthorizationRequest
      ): Future[Boolean] = Future.successful(true)

      override def findUser(
          maybeClientCredential: Option[ClientCredential],
          request: AuthorizationRequest
      ): Future[Option[User]] =
        Future.successful(Some(MockUser(10000, "username")))

      override def createAccessToken(
          authInfo: AuthInfo[User]
      ): Future[AccessToken] = throw new Exception("Failure")

    }

    val f = TokenEndpoint.handleRequest(request, dataHandler)
    f.onComplete {
      case Success(_) => fail("Must be failed by createAccessToken")
      case Failure(e) => e.getMessage should be("Failure")
    }
  }

  it should "be possible to customize supporting grant types" in {

    object TestTokenEndpoint extends TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }

    val f = TestTokenEndpoint.handleRequest(
      new AuthorizationRequest(
        Map(),
        Map("grant_type" -> Seq("client_credentials"))
      ),
      successfulDataHandler()
    )
    whenReady(f) { result =>
      val e = intercept[UnsupportedGrantType] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
      e.description should be("client_credentials is not supported")
    }
  }

  it should "be a 401 InvalidClient failure when the Authorization header is present and there is a problem extracting the client credentials" in {
    val request = new AuthorizationRequest(
      //Use Digest instead of Bearer.
      Map(
        "Authorization" -> Seq(
          "Digest Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("password"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = new MockDataHandler() {

      override def validateClient(
          maybeClientCredential: Option[ClientCredential],
          request: AuthorizationRequest
      ): Future[Boolean] = Future.successful(true)

    }

    val f = new TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      intercept[InvalidClient] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
    }
  }

  it should "be a 401 InvalidClient failure when the Authorization header is present but invalid - even when an invalid grant handler is provided" in {
    val request = new AuthorizationRequest(
      //Use Digest instead of Bearer.
      Map(
        "Authorization" -> Seq(
          "Digest Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU="
        )
      ),
      Map(
        "grant_type" -> Seq("made_up_grant"),
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )

    val dataHandler = new MockDataHandler() {

      override def validateClient(
          maybeClientCredential: Option[ClientCredential],
          request: AuthorizationRequest
      ): Future[Boolean] = Future.successful(true)

    }

    val f = new TokenEndpoint {
      override val handlers = Map("password" -> new Password())
    }.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      intercept[InvalidClient] {
        result match {
          case Left(e) => throw e
          case _       =>
        }
      }
    }
  }
}
