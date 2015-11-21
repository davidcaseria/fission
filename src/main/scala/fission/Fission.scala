package fission

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import fission.Fission.{Authenticator, RequestMapper}
import fission.auth.Principal
import org.json4s.{DefaultFormats, native}
import scaldi.akka.AkkaInjectable
import scaldi.{Injector, Module}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author David Caseria
  */
class Fission(implicit inj: Injector, system: ActorSystem) extends AkkaInjectable {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val formats = DefaultFormats
  implicit val serialization = native.Serialization
  implicit val timeout = Timeout(30 seconds)

  val authenticator = inject[Authenticator]
  val requestMapper = inject[RequestMapper]

  def routes(implicit executionContext: ExecutionContext, materializer: ActorMaterializer) = {
    import Json4sSupport._

    authenticateOAuth2(system.name, authenticator) { user =>
      pathSingleSlash {
        (post & decodeRequest & entity(as[Request])) { request =>
          val command = requestMapper(request)
          if (!user.authorize(command)) {
            complete(StatusCodes.Forbidden)
          } else {
            complete(command)
          }
        }
      }
    }
  }

  val port = inject[Int]('port)
  Http().bindAndHandle(routes, "localhost", port)
}

object Fission {

  type Authenticator = Credentials => Option[Principal]

  type RequestMapper = Request => Command

  val module = new Module {
    binding identifiedBy 'port to 8080
  }

  def apply()(implicit inj: Injector, system: ActorSystem): Fission = new Fission()(inj, system)
}
