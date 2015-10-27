package fission.reactor

import akka.actor.{Actor, ActorRef, Props}
import fission.message.{Command, Request}

class Reactor(reactions: PartialFunction[Request, (Command, ActorRef)]) extends Actor {

  override def receive = {
    case request: Request =>
      println("In the reactor")
      val (command, moderator) = reactions(request)
      moderator forward command
  }
}

object Reactor {
  def props(reactions: PartialFunction[Request, (Command, ActorRef)]) = Props(new Reactor(reactions))
}
