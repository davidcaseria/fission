package fission.message

import akka.pattern.ask
import akka.util.Timeout
import fission.reactor.Reaction.Reaction
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

case class Request(method: String, params: Option[JValue], id: Option[String], jsonrpc: Option[String])

object Request {

  implicit class RequestReactor(request: Request) {

    implicit val formats = DefaultFormats

    def react[A](reaction: Reaction)(implicit auth: Option[A], ec: ExecutionContext, t: Timeout): Future[JValue] = {
      val (command, reactor) = reaction(request)
      val response = ("jsonrpc" -> "2.0") ~ ("id" -> request.id)
      (reactor ? (command, auth)).mapTo[Response].collect({
        case Ack(result) =>
          response ~ ("result" -> result)
        case Nack(code, message, data) =>
          response ~ ("error" -> (("code" -> code) ~ ("message" -> message) ~ ("data" -> data)))
      })
    }
  }
}
