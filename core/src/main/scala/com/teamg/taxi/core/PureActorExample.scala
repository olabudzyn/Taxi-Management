package com.teamg.taxi.core

import akka.actor.{ActorSystem, Props}
import com.teamg.taxi.core.actors.TaxiSystemActor
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.StartM


class PureActorExample {
  private val system: ActorSystem = ActorSystem("TaxiManagement")

  private val taxiSystemActor = system.actorOf(Props(classOf[TaxiSystemActor], List("1","2","3")), "manager")
  taxiSystemActor ! StartM
}



