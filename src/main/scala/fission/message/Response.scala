package fission.message

import org.json4s.JsonAST.JValue
import org.json4s.native.Serialization

sealed trait Response

case class Ack(result: JValue) extends Response

object Ack {
  import org.json4s._
  implicit val formats = Serialization.formats(NoTypeHints)

  def apply(result: Any) = new Ack(Extraction.decompose(result))
}

case class Nack(code: Int, message: String, data: Option[JValue]) extends Response

object ParseError extends Nack(-32700, "Invalid JSON was received by the server", None)

object InvalidRequest extends Nack(-32600, "The JSON sent is not a valid Request object", None)

object MethodNotFound extends Nack(-32601, "The method does not exist / is not available", None)

object InvalidParams extends Nack(-32602, "Invalid method parameter(s)", None)

object InternalError extends Nack(-32603, "Internal JSON-RPC error", None)
