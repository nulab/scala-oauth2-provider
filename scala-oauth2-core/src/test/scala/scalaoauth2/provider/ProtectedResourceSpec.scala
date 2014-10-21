package scalaoauth2.provider

import java.util.Date

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProtectedResourceSpec extends FlatSpec with ScalaFutures {

  def successfulProtectedResourceHandler() = new ProtectedResourceHandler[User] {

    override def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(Some(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new Date())))

    override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = Future.successful(Some(
      AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None)
    ))

  }

  it should "be handled request with token into header" in {
    val request = ProtectedResourceRequest(
      Map("Authorization" -> Seq("OAuth token1")),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulProtectedResourceHandler()
    ProtectedResource.handleRequest(request, dataHandler).map(_ should be ('right))
  }

  it should "be handled request with token into body" in {
    val request = ProtectedResourceRequest(
      Map(),
      Map("access_token" -> Seq("token1"), "username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulProtectedResourceHandler()
    ProtectedResource.handleRequest(request, dataHandler).map(_ should be ('right))
  }

  it should "be lost expired" in {
    val request = ProtectedResourceRequest(
      Map("Authorization" -> Seq("OAuth token1")),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = new ProtectedResourceHandler[User] {

      override def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(Some(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new Date(new Date().getTime() - 4000 * 1000))))

      override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[MockUser]]] = Future.successful(Some(
        AuthInfo(user = MockUser(10000, "username"), clientId = Some("clientId1"), scope = Some("all"), redirectUri = None)
      ))

    }

    val f = ProtectedResource.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      intercept[ExpiredToken] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
    }
  }

  it should "be invalid request without token" in {
    val request = ProtectedResourceRequest(
      Map(),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = successfulProtectedResourceHandler()
    val f = ProtectedResource.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[InvalidRequest] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
      e.description should be ("Access token is not found")
    }
  }

  it should "be invalid request when not find an access token" in {
    val request = ProtectedResourceRequest(
      Map("Authorization" -> Seq("OAuth token1")),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = new ProtectedResourceHandler[User] {

      override def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(None)

      override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[MockUser]]] = Future.successful(None)

    }

    val f = ProtectedResource.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[InvalidToken] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
      e.description should be ("The access token is not found")
    }
  }

  it should "be invalid request when not find AuthInfo by token" in {
    val request = ProtectedResourceRequest(
      Map("Authorization" -> Seq("OAuth token1")),
      Map("username" -> Seq("user"), "password" -> Seq("pass"), "scope" -> Seq("all"))
    )

    val dataHandler = new ProtectedResourceHandler[User] {

      override def findAccessToken(token: String): Future[Option[AccessToken]] = Future.successful(Some(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new Date())))

      override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[MockUser]]] = Future.successful(None)

    }

    val f = ProtectedResource.handleRequest(request, dataHandler)

    whenReady(f) { result =>
      val e = intercept[InvalidToken] {
        result match {
          case Left(e) => throw e
          case _ =>
        }
      }
      e.description should be ("The access token is invalid")
    }
  }
}
