package fission.message

import fission.model.Event
import fission.http.Response
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.{DefaultFormats, Extraction}

/**
  * @author David Caseria
  */
trait Message {
  def toResponse(id: Option[String]): Response
}

case class Ack(event: Event) extends Message {
  override def toResponse(id: Option[String]): Response = {
    implicit val formats = DefaultFormats
    Response(Some(Extraction.decompose(event)), None, id)
  }
}

case class Nack(code: Int, message: String, data: Option[JValue]) extends Message {
  override def toResponse(id: Option[String]): Response = {
    implicit val formats = DefaultFormats
    Response(None, Some(("code" -> code) ~ ("message" -> message) ~ ("data" -> data)), id)
  }
}

object ParseError extends Nack(-32700, "An error occurred on the server while parsing the JSON text", None)

object InvalidRequest extends Nack(-32600, "The JSON sent is not a valid Request object", None)

object MethodNotFound extends Nack(-32601, "The method does not exist / is not available", None)

object InvalidParams extends Nack(-32602, "Invalid method parameter(s)", None)

object InternalError extends Nack(-32603, "Internal JSON-RPC error", None)
