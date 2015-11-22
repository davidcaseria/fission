package fission.model

/**
  * @author David Caseria
  */
trait State {
  def update: PartialFunction[Event, Unit]
}
