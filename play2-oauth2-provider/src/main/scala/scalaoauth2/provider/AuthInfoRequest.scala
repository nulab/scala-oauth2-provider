package scalaoauth2.provider

import play.api.mvc.{ Request, WrappedRequest }

case class AuthInfoRequest[A, U](authInfo: AuthInfo[U], private val request: Request[A]) extends WrappedRequest[A](request)
