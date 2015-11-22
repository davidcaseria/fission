import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.Credentials
import fission.Fission.{Authenticator, RequestMapper}
import fission._
import fission.auth.Principal
import fission.model.{State, Aggregate, Event}
import fission.http.Request
import fission.message.{Ack, Command}
import fission.router.Router
import scaldi.Module
import scaldi.akka.AkkaInjectable

/**
  * @author David Caseria
  */
object Main extends App with AkkaInjectable {
  implicit val module = new AppModule :: Fission.module

  implicit val system = inject[ActorSystem]

  Fission()
}

class AppModule extends Module {
  bind[ActorSystem] to ActorSystem("FissionExample") destroyWith (_.terminate())

  bind[Authenticator] to ((credentials: Credentials) => {
    credentials match {
      case provided@Credentials.Provided(token) => Some(User(token))
      case _ => None
    }
  })

  bind[RequestMapper] to ((request: Request) => {
    request.method match {
      case "SendMessage" => request.mapTo[SendMessage]
      case "ReadMessage" => request.mapTo[ReadMessage]
    }
  })

  binding toProvider new Router() {
    val messageAggregate = injectActorRef[MessageAggregate]

    override def route = {
      case _: SendMessage => messageAggregate
      case _: ReadMessage => messageAggregate
    }
  }

  binding toProvider new MessageAggregate
}

class MessageAggregate extends Aggregate(new MessageState()) {
  override def persistenceId: String = "message-aggregate"

  override def receiveCommand: Receive = {
    case SendMessage(message) => persist(MessageSent(message))(sender() ! Ack(_))
    case ReadMessage(reason) => persist(MessageRead(state.currentMessage, reason))(sender() ! Ack(_))
  }
}

class MessageState extends State {
  var currentMessage = ""
  override def update = {
    case MessageSent(message) => currentMessage = message
    case _ =>
  }
}

case class MessageSent(message: String) extends Event

case class SendMessage(message: String) extends Command

case class ReadMessage(reason: Option[String]) extends Command

case class MessageRead(message: String, reason: Option[String]) extends Event

case class User(token: String) extends Principal {
  override def authorize(command: Command): Boolean = true
}
