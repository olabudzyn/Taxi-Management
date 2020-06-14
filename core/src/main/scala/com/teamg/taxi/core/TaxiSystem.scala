package com.teamg.taxi.core


import akka.actor.{ActorSystem, Props}
import com.teamg.taxi.core.actors.TaxiSystemActor
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartM, StopM}


class TaxiSystem {

  private implicit val system: ActorSystem = ActorSystem("TaxiManagement")
  implicit val executionContext = system.dispatcher

  val taxiLabels = List("Taxi1", "Taxi2", "Taxi3", "Taxi4", "Taxi5", "Taxi6", "Taxi7", "Taxi8", "Taxi9", "Taxi10")
  private val taxiSystemActor = system.actorOf(Props(classOf[TaxiSystemActor], taxiLabels), "manager")

  def start(): Unit = {
    taxiSystemActor ! StartM
  }

  def stop(): Unit = {
    taxiSystemActor ! StopM
  }
}



