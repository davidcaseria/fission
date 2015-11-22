package fission.auth

import fission.message.Command

/**
  * @author David Caseria
  */
trait Principal {
  def authorize: PartialFunction[Command, Boolean]
}
