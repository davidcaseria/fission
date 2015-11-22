package fission.http

import fission.message.Command
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JValue

/**
  * @author David Caseria
  */
case class Request(method: String, params: Option[JValue], id: Option[String], jsonrpc: Option[String]) {
  implicit val formats = DefaultFormats

  def mapTo[T <: Command](implicit manifest: Manifest[T]): T = {
    if (params.isDefined) params.get.extract[T] else manifest.runtimeClass.asInstanceOf[T]
  }
}
