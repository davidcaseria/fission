import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import fission.Fission
import fission.message.{Ack, Command, Request}
import org.json4s._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val timeout = Timeout(5 seconds)

  implicit val formats = DefaultFormats

  val testActor = system.actorOf(Props[TestActor], "test-actor")

  Fission({
    case Request("test", params, _, _) => (params.get.extract[TestCommand], testActor)
  })
}

case class TestCommand(message: String) extends Command

class TestActor extends Actor {
  override def receive: Receive = {
    case TestCommand(message) =>
      println(message)
      sender() ! Ack(JString(message))
  }
}
