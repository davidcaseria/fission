package fission.http

import org.json4s.JsonAST.JValue

/**
  * @author David Caseria
  */
case class Response(result: Option[JValue], error: Option[JValue], id: Option[String], jsonrpc: String = "2.0")
