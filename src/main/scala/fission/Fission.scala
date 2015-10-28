package fission

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import fission.message.Request
import fission.message.Request._
import fission.reactor.Reaction.Reaction
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, native}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Fission(reactions: PartialFunction[String, Reaction])
             (implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext) {

  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(5 seconds)

  val routes = {

    import Directives._
    import Json4sSupport._

    implicit val serialization = native.Serialization // or native.Serialization
    implicit val formats = DefaultFormats

    pathSingleSlash {
      get {
        handleWebsocketMessages(Flow[Message].collect({
          case TextMessage.Strict(msg) => parse(msg).extract[Request]
        })
          .via(Flow[Request].mapAsync(4)(request => request.react(reactions(request.method))))
          .via(Flow[JValue].map(response => TextMessage.Strict(compact(render(response))))))
      } ~
        (post & decodeRequest & entity(as[Request])) { request =>
        completeOrRecoverWith(request.react(reactions(request.method))) { extraction =>
          failWith(extraction)
        }
      }
    }
  }

  Http().bindAndHandle(routes, "localhost", 8080)
}

object Fission {

  implicit val formats = DefaultFormats

  def apply(reactions: PartialFunction[String, Reaction])
           (implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext) = {
    new Fission(reactions)
  }
}
