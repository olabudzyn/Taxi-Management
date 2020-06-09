package com.teamg.taxi.actors

import akka.actor.{Actor, PoisonPill}

class OrderAllocationManagerActor extends Actor {

  var countDown = 100
  def receive = {
    case Order =>
      println(s"${self.path} manager otrzymal zamowienie, count down $countDown")

      if (countDown > 0) {
        countDown -= 1
        sender() ! NewOrder
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
    case ReportLocation =>
      println(s"${self.path} manager otrzymal lokalizacje")
  }

}

