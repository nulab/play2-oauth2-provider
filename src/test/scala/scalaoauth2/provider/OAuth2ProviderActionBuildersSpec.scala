package scalaoauth2.provider

import javax.inject.Inject

import org.scalatest._
import org.scalatest.Matchers._
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.FakeRequest

class OAuth2ProviderActionBuildersSpec extends FlatSpec {

  class MyController @Inject() (components: ControllerComponents)
      extends AbstractController(components)
      with OAuth2ProviderActionBuilders {

    val action = AuthorizedAction(new MockDataHandler) { request =>
      Ok(request.authInfo.user.name)
    }

  }

  it should "return BadRequest" in {
    val controller = new MyController(Helpers.stubControllerComponents())
    val result = controller.action(FakeRequest())
    status(result) should be(400)
    contentAsString(result) should be("")
  }

}
