package scalaoauth2.provider

case class ProtectedResourceRequest(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) extends RequestBase(headers, params) {

  def oauthToken: Option[String] = param("oauth_token")

  def accessToken: Option[String] = param("access_token")

  def requireAccessToken: String = requireParam("access_token")
}
