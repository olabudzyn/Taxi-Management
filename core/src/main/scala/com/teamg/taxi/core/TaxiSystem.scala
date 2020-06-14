package com.teamg.taxi.core


import akka.actor.{ActorSystem, Props}
import com.teamg.taxi.core.actors.TaxiSystemActor
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartM, StopM}
import com.teamg.taxi.core.api.{OrderService, TaxiSystemState}
import com.teamg.taxi.core.factory.{HttpOrderSender, HttpTaxiStateSystemReceiver, OrderSender, TaxiStateSystemReceiver}

import scala.concurrent.{ExecutionContext, Future}


class TaxiSystem extends OrderSender with TaxiStateSystemReceiver {
  private implicit val system: ActorSystem = ActorSystem("TaxiManagement")

  val taxiLabels = List("Taxi1", "Taxi2", "Taxi3", "Taxi4", "Taxi5", "Taxi6", "Taxi7", "Taxi8", "Taxi9", "Taxi10")
  private val taxiSystemActor = system.actorOf(Props(classOf[TaxiSystemActor], taxiLabels), "manager")

  def start(): Unit = {
    taxiSystemActor ! StartM
  }

  def stop(): Unit = {
    taxiSystemActor ! StopM
  }

  private val serverPort: Int = 8080

  private val orderUrl = s"http://localhost:$serverPort/order"
  private val stateUrl = s"http://localhost:$serverPort/state"

  private val orderSender = new HttpOrderSender(orderUrl)
  private val stateReceiver = new HttpTaxiStateSystemReceiver(stateUrl)


  override def send(orderRequest: OrderService.OrderRequest)(implicit executionContext: ExecutionContext): Future[Unit] = orderSender.send(orderRequest)

  override def receive(implicit executionContext: ExecutionContext): Future[TaxiSystemState] = stateReceiver.receive
}



