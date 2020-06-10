package com.teamg.taxi.core.pureactors

import akka.actor.Actor

class TaxiActorExample(id: String) extends Actor {
  override def receive: Receive = {
    case _ =>
      println("Text from TaxiActorExample")

  }
}
