package fission.auth

import fission.Command

/**
  * @author David Caseria
  */
trait Principal {
  def authorize(command: Command): Boolean
}
