package fission.reactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import fission.message.{Command, Event}

abstract class Reactor[S <: State] extends PersistentActor {

  var state: S

  override def persistenceId: String = self.path.name

  override def receiveCommand = {
    case (command: Command, auth: Option[Any]) => handleCommand(auth).apply(command)
    case "snap" => saveSnapshot(state)
  }

  override def receiveRecover = {
    case event: Event => state.handleEvent(event)
    case SnapshotOffer(_, snapshot: S) => state = snapshot
  }

  def handleCommand[T]: T => PartialFunction[Command, Unit]

  def updateState(event: Event)(handler: Event => Unit) = persist(event) { event =>
    state.handleEvent(event)
    handler(event)
  }
}
