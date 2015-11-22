package fission.auth

import fission.message.Command

/**
  * @author David Caseria
  */
trait Principal {
  def authorize(command: Command): Boolean
}
