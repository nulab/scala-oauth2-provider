package scalaoauth2

package object provider {

  object OAuthGrantType {
    val AUTHORIZATION_CODE = "authorization_code"
    val REFRESH_TOKEN = "refresh_token"
    val CLIENT_CREDENTIALS = "client_credentials"
    val PASSWORD = "password"
  }

}
