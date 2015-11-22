package fission.router

import akka.actor.{Props, Actor, ActorRef}
import fission.message.{Command, MethodNotFound}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
  * @author David Caseria
  */
abstract class Router(implicit inj: Injector) extends Actor with AkkaInjectable {

  private var actorRefs = Map.empty[Props, ActorRef]

  def reference(props: Props): ActorRef = {
    if(actorRefs.isDefinedAt(props)) {
      actorRefs.get(props).get
    } else {
      val ref = context.actorOf(props)
      actorRefs = actorRefs.updated(props, ref)
      ref
    }
  }

  override def receive = {
    case command: Command if route.isDefinedAt(command) => route.apply(command) forward command
    case _ => sender() ! MethodNotFound
  }

  def route: PartialFunction[Command, ActorRef]
}
