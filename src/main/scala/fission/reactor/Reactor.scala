package fission.reactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import fission.message.{Nack, Command, Event}

abstract class Reactor[A, S <: State] extends PersistentActor {

  var state: S

  override def persistenceId: String = self.path.name

  override def receiveCommand = {
    case (command: Command, auth: Option[A]) =>
      if (authorize(auth).apply(command)) {
        handleCommand.apply(command)
      } else {
        sender() ! Nack(-32000, "Unauthorized", None)
      }
    case "snap" => saveSnapshot(state)
  }

  override def receiveRecover = {
    case event: Event => state.handleEvent(event)
    case SnapshotOffer(_, snapshot: S) => state = snapshot
  }

  def authorize(auth: Option[A]): PartialFunction[Command, Boolean] = {
    case _ => true
  }

  def handleCommand: PartialFunction[Command, Unit]

  def updateState(event: Event)(handler: Event => Unit) = persist(event) { event =>
    state.handleEvent(event)
    handler(event)
  }
}
