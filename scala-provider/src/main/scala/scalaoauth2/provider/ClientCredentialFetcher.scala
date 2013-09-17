package scalaoauth2.provider

import org.apache.commons.codec.binary.Base64

case class ClientCredential(clientId: String, clientSecret: String)

trait ClientCredentialFetcher {

  def fetch(request: Request): Option[ClientCredential] = {
    request.header("Authorization").map { authorization =>
      val decoded = Base64.decodeBase64(authorization.substring(6).getBytes)
      new String(decoded, "UTF-8").split(":") match {
        case Array(clientId, clientSecret) => Option(ClientCredential(clientId, clientSecret))
        case _ => None
      }
    }.getOrElse {
      for {
        clientId <- request.param("client_id")
        clientSecret <- request.param("client_secret")
      } yield ClientCredential(clientId, clientSecret)
    }
  }
}

object ClientCredentialFetcher extends ClientCredentialFetcher
