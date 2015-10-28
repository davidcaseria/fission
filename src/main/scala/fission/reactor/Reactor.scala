package fission.reactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import fission.message.Event

abstract class Reactor[T <: State] extends PersistentActor {

  override def persistenceId: String = self.path.name

  var state: T

  override def receiveRecover = {
    case event: Event => state.update(event)
    case SnapshotOffer(_, snapshot: T) => state = snapshot
  }

  def updateState(event: Event) = persist(event)(state.update.apply)
}
