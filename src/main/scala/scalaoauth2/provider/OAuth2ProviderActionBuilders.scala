package scalaoauth2.provider

import play.api.mvc.{ ActionBuilder, AnyContent, BaseController }

trait OAuth2ProviderActionBuilders {

  self: BaseController =>

  def AuthorizedAction[U](handler: ProtectedResourceHandler[U]): ActionBuilder[({ type L[A] = AuthInfoRequest[A, U] })#L, AnyContent] = {
    AuthorizedActionFunction(handler)(self.defaultExecutionContext) compose Action
  }

}
