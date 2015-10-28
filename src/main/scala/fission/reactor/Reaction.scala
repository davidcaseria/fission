package fission.reactor

import akka.actor.ActorRef
import fission.message.{Command, Request}
import org.json4s.DefaultFormats

object Reaction {
  type Reaction = (Request) => (Command, ActorRef)

  implicit val formats = DefaultFormats

  def apply[T <: Command](reactor: ActorRef)(implicit m: Manifest[T]): Reaction = (request: Request) => {
    if(request.params.isDefined) (request.params.get.extract[T], reactor) else (m.runtimeClass.asInstanceOf[T], reactor)
  }
}
