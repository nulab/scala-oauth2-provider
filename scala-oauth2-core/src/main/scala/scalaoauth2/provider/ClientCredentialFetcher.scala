package scalaoauth2.provider

import org.apache.commons.codec.binary.Base64

case class ClientCredential(clientId: String, clientSecret: String)

trait ClientCredentialFetcher {

  val REGEXP_AUTHORIZATION = """^\s*Basic\s+(.+?)\s*$""".r

  def fetch(request: AuthorizationRequest): Option[ClientCredential] = {
    request.header("Authorization").flatMap {
      REGEXP_AUTHORIZATION.findFirstMatchIn
    } match {
      case Some(matcher) =>
        val authorization = matcher.group(1)
        val decoded = new String(Base64.decodeBase64(authorization.getBytes), "UTF-8")
        if (decoded.indexOf(':') > 0) {
          decoded.split(":", 2) match {
            case Array(clientId, clientSecret) => Some(ClientCredential(clientId, clientSecret))
            case Array(clientId) => Some(ClientCredential(clientId, ""))
            case _ => None
          }
        } else {
          None
        }
      case _ => request.clientId.map { clientId =>
        ClientCredential(clientId, request.clientSecret.getOrElse(""))
      }
    }
  }
}

object ClientCredentialFetcher extends ClientCredentialFetcher
