package fission.reactor

import fission.message.Event

abstract class State {
  def handleEvent: PartialFunction[Event, Unit]
}
