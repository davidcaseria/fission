package fission.reactor

import fission.message.Event

abstract class State {
  def update: PartialFunction[Event, Unit]
}
