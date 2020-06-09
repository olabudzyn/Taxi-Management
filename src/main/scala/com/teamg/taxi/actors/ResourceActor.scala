package com.teamg.taxi.actors
import akka.actor.{Actor, ActorRef}


/* agent representing the driver */
class ResourceActor(manager: ActorRef) extends Actor {
  def receive = {
    case Disposition =>
      println(s"${self.path} taksowka otrzymanala dyspozycje")
      //manager ! Order
    case ReportLocation =>
      manager ! ReportLocation
  }


}

