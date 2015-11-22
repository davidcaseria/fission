package fission.model

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}

/**
  * @author David Caseria
  */
abstract class Entity[T <: State](var state: T) extends PersistentActor with ActorLogging {

  def persist(event: Event)(callback: Event => Unit) = {
    super.persist(event) { event =>
      state.update.apply(event)
      callback(event)
    }
  }

  override def receiveRecover: Receive = {
    case event: Event => state.update(event)
    case SnapshotOffer(_, snapshot: T) => state = snapshot
  }
}
