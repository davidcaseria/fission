package fission.router

import akka.actor.Actor
import fission.{Message, Command}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
  * @author David Caseria
  */
abstract class Router(implicit inj: Injector) extends Actor with AkkaInjectable {

  override def receive = {
    case command: Command => sender() ! receiveCommand.apply(command)
  }

  def receiveCommand: PartialFunction[Command, Message]
}
