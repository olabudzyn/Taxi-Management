package com.teamg.taxi.core

import akka.actor.ActorSystem
import com.teamg.taxi.core.factory.TaxiSystemStateReceiver

object Main extends App {

  val actorExample = new TaxiSystem()
  implicit val actorSystem = ActorSystem("Main")
  Thread.sleep(10000)
  import actorSystem.dispatcher

  val taxiSystemStateReceiver = new TaxiSystemStateReceiver("http://localhost:8080/state")
  val response = taxiSystemStateReceiver.receive

  Thread.sleep(100000)
}
