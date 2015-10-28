import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import fission.Fission
import fission.message.{Ack, Command, Event}
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

  override def update: PartialFunction[Event, Unit] = {
    case NameUpdated(newName) =>
      name = newName
    case _ => println("Idk...")
  }
}

class TestReactor extends Reactor[TestState] {

  import org.json4s.JsonDSL._
  
  override var state: TestState = new TestState()

  override def receiveCommand: Receive = {
    case UpdateName(name) =>
      val oldName = state.name
      persist(NameUpdated(name)) { event =>
        state.update(event)
        sender() ! Ack(("oldName" -> oldName) ~ ("newName" -> state.name))
      }
  }
}
