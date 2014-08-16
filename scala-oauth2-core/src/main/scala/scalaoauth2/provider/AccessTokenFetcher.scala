package scalaoauth2.provider

import java.net.URLDecoder

case class FetchResult(token: String, params: Map[String, String])

trait AccessTokenFetcher {

  def matches(request: ProtectedResourceRequest): Boolean

  def fetch(request: ProtectedResourceRequest): FetchResult

}

object RequestParameter extends AccessTokenFetcher {

  override def matches(request: ProtectedResourceRequest): Boolean = {
    request.oauthToken.isDefined || request.accessToken.isDefined
  }

  override def fetch(request: ProtectedResourceRequest): FetchResult = {
    val t = request.oauthToken.getOrElse(request.requireAccessToken)
    val params = request.params.filter { case (k, v) => !v.isEmpty } map { case (k, v) => (k, v.head) }
    FetchResult(t, params - ("oauth_token", "access_token"))
  }
}

object AuthHeader extends AccessTokenFetcher {
  val REGEXP_AUTHORIZATION = """^\s*(OAuth|Bearer)\s+([^\s\,]*)""".r
  val REGEXP_TRIM = """^\s*,\s*""".r
  val REGEXP_DIV_COMMA = """,\s*""".r

  override def matches(request: ProtectedResourceRequest): Boolean = {
    request.header("Authorization").exists { header =>
      REGEXP_AUTHORIZATION.findFirstMatchIn(header).isDefined
    }
  }

  override def fetch(request: ProtectedResourceRequest): FetchResult = {
    val header = request.requireHeader("Authorization")
    val matcher = REGEXP_AUTHORIZATION.findFirstMatchIn(header).getOrElse {
      throw new InvalidRequest("Authorization is invalid")
    }

    val token = matcher.group(2)
    val end = matcher.end
    val params = if (header.length != end) {
      val trimmedHeader = REGEXP_TRIM.replaceFirstIn(header.substring(end), "")
      val pairs = REGEXP_DIV_COMMA.split(trimmedHeader).map { exp =>
        val (key, value) = exp.split("=", 2) match {
          case Array(k, v) => (k, v.replaceFirst("^\"", ""))
          case Array(k) => (k, "")
        }

        (key, URLDecoder.decode(value.replaceFirst("\"$", ""), "UTF-8"))
      }

      Map(pairs: _*)
    } else {
      Map.empty[String, String]
    }

    FetchResult(token, params)
  }
}
