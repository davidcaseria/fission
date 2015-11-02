import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import fission.Fission
import fission.message.{Nack, Ack, Command, Event}
import fission.reactor.{Reaction, Reactor, State}
import org.json4s._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val timeout = Timeout(5 seconds)

  implicit val formats = DefaultFormats

  val reactor = system.actorOf(Props[TestReactor], "test-reactor")

  Fission({
    case "updateName" => Reaction[UpdateName](reactor)
  })
}

case class UpdateName(name: String) extends Command

case class NameUpdated(name: String) extends Event

class TestState extends State {

  var name: String = "World"

  override def handleEvent = {
    case NameUpdated(newName) => name = newName
  }
}

class TestReactor extends Reactor[String, TestState] {

  override var state: TestState = new TestState()

  override def authorize(auth: Option[String]) = {
    case UpdateName(name) => auth.isDefined && auth.get == name
  }

  override def handleCommand = {
    case UpdateName(name) =>
      if(state.name == name) {
        sender() ! Nack(-32000, "Must change the name", Some(JString(name)))
      } else {
        val oldName = state.name
        updateState(NameUpdated(name)) { event =>
          sender() ! Ack(s"Goodbye $oldName, Hello ${state.name}")
        }
      }
  }
}
