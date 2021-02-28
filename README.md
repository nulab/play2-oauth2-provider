# play2-oauth2-provider [![CI](https://github.com/nulab/play2-oauth2-provider/actions/workflows/ci.yml/badge.svg)](https://github.com/nulab/play2-oauth2-provider/actions/workflows/ci.yml)

This library is enabled using [scala-oauth2-provider](https://github.com/nulab/scala-oauth2-provider) in Play Framework.

## Setup

Add "play2-oauth2-provider" to library dependencies of your project.

```scala
libraryDependencies ++= Seq(
  "com.nulab-inc" %% "scala-oauth2-core" % "1.5.0",
  "com.nulab-inc" %% "play2-oauth2-provider" % "1.5.0"
)
```

Library version | Play version
--------------- | ------------
1.5.0           | 2.8.x
1.4.2           | 2.7.x
1.3.0           | 2.6.x
1.2.0           | 2.5.x
0.16.1          | 2.4.x
0.14.0          | 2.3.x
0.7.4           | 2.2.x

## How to use

You should follow four steps below to work with Play Framework.

* Customizing Grant Handlers
* Define a controller to issue access token
* Assign a route to the controller
* Access to an authorized resource

You want to use which grant types are supported or to use a customized handler for a grant type, you should override the ```handlers``` map in a customized ```TokenEndpoint``` trait.

```scala
class MyTokenEndpoint extends TokenEndpoint {
  override val handlers = Map(
    OAuthGrantType.AUTHORIZATION_CODE -> new AuthorizationCode(),
    OAuthGrantType.REFRESH_TOKEN -> new RefreshToken(),
    OAuthGrantType.CLIENT_CREDENTIALS -> new ClientCredentials(),
    OAuthGrantType.PASSWORD -> new Password(),
    OAuthGrantType.IMPLICIT -> new Implicit()
  )
}
```

Here's an example of a customized ```TokenEndpoint``` that 1) only supports the ```password``` grant type, and 2) customizes the ```password``` grant type handler to not require client credentials:

```scala
class MyTokenEndpoint extends TokenEndpoint {
  val passwordNoCred = new Password() {
    override def clientCredentialRequired = false
  }

  override val handlers = Map(
    OAuthGrantType.PASSWORD -> passwordNoCred
  )
}
```

Define your own controller with mixining ```OAuth2Provider``` trait provided by this library to issue access token with customized `TokenEndpoint`.

```scala
class MyController @Inject() (components: ControllerComponents)
  extends AbstractController(components) with OAuth2Provider {
  override val tokenEndpoint = new MyTokenEndpoint()

  def accessToken = Action.async { implicit request =>
    issueAccessToken(new MyDataHandler())
  }
}
```

Then, assign a route to the controller that OAuth clients will access to.

```
POST    /oauth2/access_token                    controllers.OAuth2Controller.accessToken
```

Finally, you can access to an authorized resource like this:

```scala
class MyController @Inject() (components: ControllerComponents)
  extends AbstractController(components) with OAuth2Provider {

  val action = Action.async { request =>
    authorize(new MockDataHandler()) { authInfo =>
      val user = authInfo.user // User is defined on your system
      // access resource for the user
      ???
    }
  }
}
```

If you'd like to change the OAuth workflow, modify handleRequest methods of `TokenEndPoint` and `ProtectedResource` traits.

### Using Action composition

You can write more easily authorize action by using Action composition.

Play Framework's documentation is [here](https://www.playframework.com/documentation/2.7.x/ScalaActionsComposition).

```scala
class MyController @Inject() (components: ControllerComponents)
  extends AbstractController(components) with OAuth2ProviderActionBuilders {

  def list = AuthorizedAction(new MyDataHandler()) { request =>
    val user = request.authInfo.user // User is defined on your system
    // access resource for the user
  }
}
```

## Examples

### Play Framework 2.5

- https://github.com/lglossman/scala-oauth2-deadbolt-redis
- https://github.com/tsuyoshizawa/scala-oauth2-provider-example-skinny-orm

### Play Framework 2.3

- https://github.com/davidseth/scala-oauth2-provider-slick

### Play Framework 2.2

- https://github.com/oyediyildiz/scala-oauth2-provider-example
- https://github.com/tuxdna/play-oauth2-server
