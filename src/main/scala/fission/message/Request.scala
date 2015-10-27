package fission.message

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

case class Request(method: String, params: Option[JValue], id: Option[String], jsonrpc: Option[String])

object RequestSender {

  implicit class Sender(request: Request) {

    implicit val formats = DefaultFormats

    def send(reactor: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout): Future[JValue] = {
      val response = ("jsonrpc" -> "2.0") ~ ("id" -> request.id)
      (reactor ? request).mapTo[Response].collect({
        case Ack(result) =>
          response ~ ("result" -> result)
        case Nack(code, message, data) =>
          response ~ ("error" -> (("code" -> code) ~ ("message" -> message) ~ ("data" -> data)))
      })
    }
  }
}
