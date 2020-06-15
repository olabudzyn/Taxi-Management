package com.teamg.taxi.core

import akka.actor.ActorSystem
import com.teamg.taxi.core.api.OrderService
import com.teamg.taxi.core.factory.{HttpOrderSender, OrderSender}

import scala.concurrent.{ExecutionContext, Future}

class SimulationOrderSender(url: String) extends OrderSender{
  private implicit val actorSystem: ActorSystem = ActorSystem("OrderSender")

  private val sender = new HttpOrderSender(url)

  override def send(orderRequest: OrderService.OrderRequest)
                   (implicit executionContext: ExecutionContext): Future[Unit] = sender.send(orderRequest)
}
