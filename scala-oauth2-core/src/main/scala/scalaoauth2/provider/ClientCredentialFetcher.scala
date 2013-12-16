package scalaoauth2.provider

import org.apache.commons.codec.binary.Base64

case class ClientCredential(clientId: String, clientSecret: String)

trait ClientCredentialFetcher {

  def fetch(request: AuthorizationRequest): Option[ClientCredential] = {
    request.header("Authorization") match {
      case Some(authorization) if authorization.length > 5 => {
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
      }
      case _ => request.clientId.map { clientId =>
        ClientCredential(clientId, request.clientSecret.getOrElse(""))
      }
    }
  }
}

object ClientCredentialFetcher extends ClientCredentialFetcher
