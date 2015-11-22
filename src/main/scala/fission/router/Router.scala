package fission.router

import akka.actor.{Actor, ActorRef}
import fission.message.{Command, MethodNotFound}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
  * @author David Caseria
  */
abstract class Router(implicit inj: Injector) extends Actor with AkkaInjectable {

  override def receive = {
    case command: Command if route.isDefinedAt(command) => route.apply(command) forward command
    case _ => sender() ! MethodNotFound
  }

  def route: PartialFunction[Command, ActorRef]
}
