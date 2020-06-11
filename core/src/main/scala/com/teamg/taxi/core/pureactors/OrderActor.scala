package com.teamg.taxi.core.pureactors

import akka.actor.Actor
import com.teamg.taxi.core.model.Order

class OrderActor(order: Order) extends Actor{
  override def receive: Receive = {
    case _ => println("Received")
  }
}
