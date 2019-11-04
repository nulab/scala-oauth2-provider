package scalaoauth2.provider

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PasswordSpec extends FlatSpec with ScalaFutures with OptionValues {

  val passwordClientCredReq = new Password()
  val passwordNoClientCredReq = new Password() {
    override def clientCredentialRequired = false
  }

  "Password when client credential required" should "handle request" in handlesRequest(
    passwordClientCredReq,
    Map(
      "client_id" -> Seq("clientId1"),
      "client_secret" -> Seq("clientSecret1")
    )
  )
  "Password when client credential not required" should "handle request" in handlesRequest(
    passwordNoClientCredReq,
    Map.empty
  )

  def handlesRequest(password: Password, params: Map[String, Seq[String]]) = {
    val request = new AuthorizationRequest(
      Map(),
      params ++ Map(
        "username" -> Seq("user"),
        "password" -> Seq("pass"),
        "scope" -> Seq("all")
      )
    )
    val clientCred = request.parseClientCredential
      .fold[Option[ClientCredential]](None)(_.fold(_ => None, c => Some(c)))
    val f = password.handleRequest(
      clientCred,
      request,
      new MockDataHandler() {

        override def findUser(
            maybeClientCredential: Option[ClientCredential],
            request: AuthorizationRequest
        ): Future[Option[User]] =
          Future.successful(Some(MockUser(10000, "username")))

        override def createAccessToken(
            authInfo: AuthInfo[User]
        ): Future[AccessToken] =
          Future.successful(
            AccessToken(
              "token1",
              Some("refreshToken1"),
              Some("all"),
              Some(3600),
              new java.util.Date()
            )
          )

      }
    )

    whenReady(f) { result =>
      result.tokenType should be("Bearer")
      result.accessToken should be("token1")
      result.expiresIn.value should (be <= 3600L and be > 3595L)
      result.refreshToken should be(Some("refreshToken1"))
      result.scope should be(Some("all"))
    }
  }
}
