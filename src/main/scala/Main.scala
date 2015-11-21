import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.Credentials
import fission.router.Router
import fission._
import fission.Fission.{RequestMapper, Authenticator}
import fission.auth.Principal
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
    }
  })

  binding toProvider new Router() {
    override def receiveCommand: PartialFunction[Command, Message] = {
      case _ => MethodNotFound
    }
  }
}

case class SendMessage(message: String) extends Command

case class User(token: String) extends Principal {
  override def authorize(command: Command): Boolean = true
}
