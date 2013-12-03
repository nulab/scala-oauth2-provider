package scalaoauth2.provider

import org.apache.commons.codec.binary.Base64

case class ClientCredential(clientId: String, clientSecret: String)

trait ClientCredentialFetcher {

  def fetch(request: Request): Option[ClientCredential] = {
    request.header("Authorization").map { authorization =>
      val decoded = new String(Base64.decodeBase64(authorization.substring(6).getBytes), "UTF-8")
      if (decoded.indexOf(':') > 0) {
        decoded.split(":", 2) match {
          case Array(clientId, clientSecret) => Option(ClientCredential(clientId, clientSecret))
          case Array(clientId) => Option(ClientCredential(clientId, ""))
          case _ => None
        }
        
      } else {
        None
      }

    }.getOrElse {
      request.param("client_id").map(clientId =>
        ClientCredential(clientId, request.param("client_secret").getOrElse("")))
    }
  }
}

object ClientCredentialFetcher extends ClientCredentialFetcher
