package com.teamg.taxi.core


import akka.actor.{ActorRef, ActorSystem, Props}
import com.teamg.taxi.core.actors.TaxiSystemActor
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.StopM
import com.teamg.taxi.core.api.TaxiSystemState
import com.teamg.taxi.core.factory.{HttpTaxiStateSystemReceiver, TaxiStateSystemReceiver}

import scala.concurrent.{ExecutionContext, Future}

class TaxiSystem(config: SimulationConfig) extends TaxiStateSystemReceiver {
  private implicit val system: ActorSystem = ActorSystem("TaxiSystemRoot")

  private val taxiSystemActor: ActorRef = system.actorOf(Props(classOf[TaxiSystemActor], config), "manager")

  private val stateReceiver = new HttpTaxiStateSystemReceiver(ServiceConfig.stateUrl)

  def stop(): Unit = {
    taxiSystemActor ! StopM
  }

  override def receive(implicit executionContext: ExecutionContext): Future[TaxiSystemState] = stateReceiver.receive

}



