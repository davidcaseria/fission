package example

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.directives.Credentials
import example.Commands.{CheckBalance, Deposit, OpenAccount, Withdraw}
import example.Events.{AccountOpened, BalanceChecked, Deposited, Withdrawn}
import fission.Fission
import fission.Fission._
import fission.auth.Principal
import fission.http.Request
import fission.message.{Command, Nack}
import fission.model.{Aggregate, Event, State}
import fission.router.Router
import org.json4s.JsonDSL._
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
  // Create an example bank system
  bind[ActorSystem] to ActorSystem("BankSystem") destroyWith (_.terminate())

  // Resolve credentials to a principal
  bind[Authenticator] to ((credentials: Credentials) => {
    credentials match {
      case provided@Credentials.Provided(token) => Some(User(token))
      case _ => None
    }
  })

  // Map requests to commands
  bind[RequestMapper] to ((request: Request) => {
    request.method match {
      case "CheckBalance" => request.mapTo[CheckBalance]
      case "Deposit" => request.mapTo[Deposit]
      case "OpenAccount" => request.mapTo[OpenAccount]
      case "Withdraw" => request.mapTo[Withdraw]
    }
  })

  // Router to forward commands to aggregates
  binding toProvider new Router() {
    override def route = {
      case CheckBalance(userId) => reference(Account.props(userId))
      case Deposit(userId, _) => reference(Account.props(userId))
      case OpenAccount(userId) => reference(Account.props(userId))
      case Withdraw(userId, _) => reference(Account.props(userId))
    }
  }
}

class Account(val id: String) extends Aggregate(new AccountState()) {
  override def persistenceId = s"account-$id"

  override def applyEvent = {
    case Deposited(amount) => state.balance + amount
    case Withdrawn(amount) => state.balance - amount
    case _ =>
  }

  override def receiveCommand = {
    case OpenAccount(userId) => acknowledge(AccountOpened(userId))
    case Deposit(_, amount) => acknowledge(Deposited(amount))
    case Withdraw(_, amount) =>
      if (state.balance > amount) {
        acknowledge(Withdrawn(amount))
      } else {
        sender() ! Nack(-32000, "Overdraft", Some(("amount" -> amount) ~ ("balance" -> state.balance)))
      }
    case CheckBalance(_) => acknowledge(BalanceChecked(state.balance))
  }
}

object Account {
  def props(id: String) = Props(new Account(id))
}

class AccountState extends State {
  var balance = BigDecimal("0.00")
}

object Commands {

  case class OpenAccount(userId: String) extends Command

  case class Deposit(userId: String, amount: BigDecimal) extends Command

  case class Withdraw(userId: String, amount: BigDecimal) extends Command

  // This is just an example. Use CQRS instead!
  case class CheckBalance(userId: String) extends Command

}

object Events {

  case class AccountOpened(userId: String) extends Event

  case class Deposited(amount: BigDecimal) extends Event

  case class Withdrawn(amount: BigDecimal) extends Event

  case class BalanceChecked(balance: BigDecimal) extends Event

}

case class User(id: String) extends Principal {
  override def authorize = {
    case CheckBalance(userId) => id == userId
    case Deposit(userId, _) => id == userId
    case OpenAccount(userId) => id == userId
    case Withdraw(userId, _) => id == userId
  }
}
