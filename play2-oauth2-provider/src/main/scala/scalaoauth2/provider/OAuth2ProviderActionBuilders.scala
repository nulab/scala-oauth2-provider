package scalaoauth2.provider

import play.api.mvc._

import scala.concurrent.{ Future, ExecutionContext }

trait OAuth2ProviderActionBuilders {

  implicit val executionContext: ExecutionContext

  def AuthorizedAction[U](handler: ProtectedResourceHandler[U]): ActionBuilder[({ type L[A] = AuthInfoRequest[A, U] })#L] = {
    AuthorizedActionFunction(handler) compose Action
  }

}

object OAuth2ProviderActionBuilders extends OAuth2ProviderActionBuilders {
  implicit val executionContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
}
