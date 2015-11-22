package fission.model

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}
import fission.message.Ack

/**
  * @author David Caseria
  */
abstract class Entity[T <: State](var state: T) extends PersistentActor with ActorLogging {

  def acknowledge(event: Event): Unit = {
    persist(event) { event =>
      applyEvent.apply(event)
      sender() ! Ack(event)
    }
  }

  def applyEvent: PartialFunction[Event, Unit]

  override def receiveRecover: Receive = {
    case event: Event => applyEvent.apply(event)
    case SnapshotOffer(_, snapshot: T) => state = snapshot
  }
}
