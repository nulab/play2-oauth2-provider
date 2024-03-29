package scalaoauth2.provider

import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/** Basic OAuth2 provider trait.
  */
trait OAuth2BaseProvider extends Results {

  private[provider] def getParam[A](
      request: Request[A]
  ): Map[String, Seq[String]] = {
    val unwrap = request.body match {
      case body: play.api.mvc.AnyContent =>
        body.asFormUrlEncoded
          .orElse(body.asMultipartFormData)
          .orElse(body.asJson)
          .getOrElse(body)
      case body => body
    }
    ((unwrap match {
      case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
      case body: MultipartFormData[_]        => body.asFormUrlEncoded
      case Right(body: MultipartFormData[_]) => body.asFormUrlEncoded
      case body: play.api.libs.json.JsValue =>
        FormUtils.fromJson(js = body).view.mapValues(Seq(_))
      case _ => Map.empty
    }) ++ request.queryString).toMap
  }

  private[provider] object FormUtils {

    import play.api.libs.json._

    def fromJson(prefix: String = "", js: JsValue): Map[String, String] =
      js match {
        case JsObject(fields) =>
          fields
            .map { case (key, value) =>
              fromJson(
                Option(prefix)
                  .filterNot(_.isEmpty)
                  .map(_ + ".")
                  .getOrElse("") + key,
                value
              )
            }
            .foldLeft(Map.empty[String, String])(_ ++ _)
        case JsArray(values) =>
          values.zipWithIndex
            .map { case (value, i) => fromJson(prefix + "[" + i + "]", value) }
            .foldLeft(Map.empty[String, String])(_ ++ _)
        case JsNull           => Map.empty
        case JsUndefined()    => Map.empty
        case JsBoolean(value) => Map(prefix -> value.toString)
        case JsNumber(value)  => Map(prefix -> value.toString)
        case JsString(value)  => Map(prefix -> value.toString)
      }

  }

  protected[scalaoauth2] def responseOAuthErrorHeader(
      e: OAuthError
  ): (String, String) =
    "WWW-Authenticate" -> ("Bearer " + toOAuthErrorString(e))

  protected def toOAuthErrorString(e: OAuthError): String = {
    val params = Seq("error=\"" + e.errorType + "\"") ++
      (if (e.description.nonEmpty) {
         Seq("error_description=\"" + e.description + "\"")
       } else {
         Nil
       })
    params.mkString(", ")
  }

}

trait OAuth2ProtectedResourceProvider extends OAuth2BaseProvider {

  val protectedResource: ProtectedResource = ProtectedResource

  implicit def play2protectedResourceRequest(
      request: RequestHeader
  ): ProtectedResourceRequest = {
    new ProtectedResourceRequest(request.headers.toMap, request.queryString)
  }

  implicit def play2protectedResourceRequest[A](
      request: Request[A]
  ): ProtectedResourceRequest = {
    val param: Map[String, Seq[String]] = getParam(request)
    new ProtectedResourceRequest(request.headers.toMap, param)
  }

  /** Authorize to already created access token in ProtectedResourceHandler
    * process and return the response to client.
    *
    * @param handler
    *   Implemented ProtectedResourceHandler for authenticate to your system.
    * @param callback
    *   Callback is called when authentication is successful.
    * @param request
    *   Play Framework is provided HTTP request interface.
    * @param ctx
    *   This contxt is used by ProtectedResource.
    * @tparam A
    *   play.api.mvc.Request has type.
    * @tparam U
    *   set the type in AuthorizationHandler.
    * @return
    *   Authentication is successful then the response use your API result.
    *   Authentication is failed then return BadRequest or Unauthorized status
    *   to client with cause into the JSON.
    */
  def authorize[A, U](handler: ProtectedResourceHandler[U])(
      callback: AuthInfo[U] => Future[Result]
  )(implicit request: Request[A], ctx: ExecutionContext): Future[Result] = {
    protectedResource.handleRequest(request, handler).flatMap {
      case Left(e) =>
        Future.successful(
          new Status(e.statusCode).withHeaders(responseOAuthErrorHeader(e))
        )
      case Right(authInfo) => callback(authInfo)
    }
  }

}

trait OAuth2TokenEndpointProvider extends OAuth2BaseProvider {

  val tokenEndpoint: TokenEndpoint = TokenEndpoint

  implicit def play2oauthRequest(
      request: RequestHeader
  ): AuthorizationRequest = {
    new AuthorizationRequest(request.headers.toMap, request.queryString)
  }

  implicit def play2oauthRequest[A](
      request: Request[A]
  ): AuthorizationRequest = {
    val param: Map[String, Seq[String]] = getParam(request)
    new AuthorizationRequest(request.headers.toMap, param)
  }

  /** Issue access token in AuthorizationHandler process and return the response
    * to client.
    *
    * @param handler
    *   Implemented AuthorizationHandler for register access token to your
    *   system.
    * @param request
    *   Play Framework is provided HTTP request interface.
    * @param ctx
    *   This context is used by TokenEndPoint.
    * @tparam A
    *   play.api.mvc.Request has type.
    * @tparam U
    *   set the type in AuthorizationHandler.
    * @return
    *   Request is successful then return JSON to client in OAuth 2.0 format.
    *   Request is failed then return BadRequest or Unauthorized status to
    *   client with cause into the JSON.
    */
  def issueAccessToken[A, U](
      handler: AuthorizationHandler[U]
  )(implicit request: Request[A], ctx: ExecutionContext): Future[Result] = {
    tokenEndpoint.handleRequest(request, handler).map {
      case Left(e) =>
        new Status(e.statusCode)(responseOAuthErrorJson(e))
          .withHeaders(responseOAuthErrorHeader(e))
      case Right(r) =>
        Ok(Json.toJson(responseAccessToken(r)))
          .withHeaders("Cache-Control" -> "no-store", "Pragma" -> "no-cache")
    }
  }

  protected[scalaoauth2] def responseOAuthErrorJson(e: OAuthError): JsValue =
    Json.obj("error" -> e.errorType, "error_description" -> e.description)

  protected[scalaoauth2] def responseAccessToken[U](
      r: GrantHandlerResult[U]
  ) = {
    Map[String, JsValue](
      "token_type" -> JsString(r.tokenType),
      "access_token" -> JsString(r.accessToken)
    ) ++ r.expiresIn.map {
      "expires_in" -> JsNumber(_)
    } ++ r.refreshToken.map {
      "refresh_token" -> JsString(_)
    } ++ r.scope.map {
      "scope" -> JsString(_)
    } ++ r.params.map(e => (e._1, JsString(e._2)))
  }

}

/** OAuth2Provider supports issue access token and authorize.
  *
  * <h3>Create controller for issue access token</h3>
  * @example
  *   {{{ object OAuth2Controller extends Controller with OAuth2Provider { def
  *   accessToken = Action.async { implicit request => issueAccessToken(new
  *   MyDataHandler()) } } }}}
  *
  * <h3>Register routes</h3>
  * @example
  *   {{{POST /oauth2/access_token controllers.OAuth2Controller.accessToken}}}
  *
  * <h3>Authorized</h3>
  * @example
  *   {{{ import scalaoauth2.provider._ object BookController extends Controller
  *   with OAuth2Provider { def list = Action.async { implicit request =>
  *   authorize(new MyDataHandler()) { authInfo => val user = authInfo.user //
  *   User is defined on your system // access resource for the user } } } }}}
  */
trait OAuth2Provider
    extends OAuth2ProtectedResourceProvider
    with OAuth2TokenEndpointProvider
