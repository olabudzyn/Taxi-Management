package com.teamg.taxi.core

import akka.actor.ActorSystem
import com.teamg.taxi.core.api.{AccidentService, OrderService}
import com.teamg.taxi.core.factory.{AccidentSender, HttpAccidentSender, HttpOrderSender, OrderSender}

import scala.concurrent.{ExecutionContext, Future}

class SimulationSender(urlOrder: String, urlAccident: String) extends OrderSender with AccidentSender{
  private implicit val actorSystem: ActorSystem = ActorSystem("OrderSender")

  private val orderSender = new HttpOrderSender(urlOrder)
  private val accidentSender = new HttpAccidentSender(urlAccident)

  override def send(orderRequest: OrderService.OrderRequest)
                   (implicit executionContext: ExecutionContext): Future[Unit] = orderSender.send(orderRequest)

  override def send(accidentRequest: AccidentService.AccidentRequest)
                   (implicit executionContext: ExecutionContext): Future[Unit] = accidentSender.send(accidentRequest)
}
