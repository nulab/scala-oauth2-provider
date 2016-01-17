package scalaoauth2.provider

import play.api.mvc._

import scala.concurrent.{ Future, ExecutionContext }

case class AuthorizedActionFunction[U](handler: ProtectedResourceHandler[U])(implicit ctx: ExecutionContext) extends ActionFunction[Request, ({ type L[A] = AuthInfoRequest[A, U] })#L] with OAuth2Provider {

  override def invokeBlock[A](request: Request[A], block: AuthInfoRequest[A, U] => Future[Result]): Future[Result] = {
    authorize(handler) { authInfo =>
      block(AuthInfoRequest(authInfo, request))
    }(request, ctx)
  }

}
