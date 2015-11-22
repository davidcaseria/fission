package fission.model

import scaldi.akka.AkkaInjectable

/**
  * @author David Caseria
  */
abstract class Aggregate[T <: State](state: T) extends Entity(state) with AkkaInjectable
