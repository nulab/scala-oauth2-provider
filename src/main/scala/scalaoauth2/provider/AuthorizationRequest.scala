package scalaoauth2.provider

import java.util.Base64

import scala.util.Try

case class ClientCredential(clientId: String, clientSecret: Option[String])

class AuthorizationRequest(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) extends RequestBase(headers, params) {

  def scope: Option[String] = param("scope")

  def grantType: String = requireParam("grant_type")

  def parseClientCredential: Option[Either[InvalidClient, ClientCredential]] =
    findAuthorization
      .flatMap(x => Some(x.fold(
        left => Left(left),
        header => clientCredentialByAuthorization(header))))
      .orElse(clientCredentialByParam.map(Right(_)))

  private def findAuthorization: Option[Either[InvalidClient, String]] = {
    header("Authorization").map { auth =>
      val basicAuthCred = for {
        matcher <- """^\s*Basic\s+(.+?)\s*$""".r.findFirstMatchIn(auth)
      } yield matcher.group(1)

      basicAuthCred.fold[Either[InvalidClient, String]](Left(new InvalidClient("Authorization header could not be parsed")))(x => Right(x))
    }
  }

  private def clientCredentialByAuthorization(s: String): Either[InvalidClient, ClientCredential] =
    Try(new String(Base64.getDecoder.decode(s), "UTF-8"))
      .map(_.split(":", 2))
      .getOrElse(Array.empty) match {
        case Array(clientId, clientSecret) =>
          Right(ClientCredential(clientId, if (clientSecret.isEmpty) None else Some(clientSecret)))
        case _ =>
          Left(new InvalidClient())
      }

  private def clientCredentialByParam = param("client_id").map(ClientCredential(_, param("client_secret")))

}

case class RefreshTokenRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
   * returns refresh_token.
   *
   * @return code.
   * @throws InvalidRequest if the parameter is not found
   */
  def refreshToken: String = requireParam("refresh_token")
}

case class PasswordRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
   * returns username.
   *
   * @return username.
   * @throws InvalidRequest if the parameter is not found
   */
  def username = requireParam("username")

  /**
   * returns password.
   *
   * @return password.
   * @throws InvalidRequest if the parameter is not found
   */
  def password = requireParam("password")
}

case class ClientCredentialsRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params)

case class AuthorizationCodeRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
  /**
   * returns code.
   *
   * @return code.
   * @throws InvalidRequest if code is not found
   */
  def code: String = requireParam("code")

  /**
   * Returns redirect_uri.
   *
   * @return redirect_uri
   */
  def redirectUri: Option[String] = param("redirect_uri")

  /**
   * Returns code_verifier
   *
   * @return
   */
  def codeVerifier: Option[String] = param("code_verifier")
}

case class ImplicitRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params)
