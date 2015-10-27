package fission

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import fission.message.RequestSender._
import fission.message.{Command, Request}
import fission.reactor.Reactor
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Fission(reactions: PartialFunction[Request, (Command, ActorRef)])
             (implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext) {

  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(5 seconds)

  val reactor = system.actorOf(Reactor.props(reactions), "reactor")

  val routes = (pathSingleSlash & get) {
    handleWebsocketMessages(Flow[Message].collect({
      case TextMessage.Strict(msg) => parse(msg).extract[Request]
    })
    .via(Flow[Request].mapAsync(4)(_.send(reactor)))
    .via(Flow[JValue].map(response => TextMessage.Strict(compact(render(response))))))
  }

  Http().bindAndHandle(routes, "localhost", 8080)
}

object Fission {
  def apply(reactions: PartialFunction[Request, (Command, ActorRef)])
           (implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContext) = new Fission(reactions)
}
