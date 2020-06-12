package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorRef, Props}
import com.teamg.taxi.core.factory.OrderFactory
import com.teamg.taxi.core.model.{Order}
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{ArrivedOrderM, CompletedOrderM, CreateOrderActorsM}

class OrderAllocationManagerActor extends Actor {

  private var orderActors = Map.empty[String, ActorRef]
  var orderFactory = OrderFactory

  override def receive: Receive = {

    case CreateOrderActorsM =>


    case ArrivedOrderM(order: Order) =>
      addOrderActor(order)
      printOrderActors()

    case CompletedOrderM(orderId: String) =>
      deleteOrderActor(orderId)
      printOrderActors()


  }

  //private def createOrder(from: String, target: String, customerType: CustomerType, orderType: OrderType) = {
  //  orderFactory.create(from, target, customerType, orderType)
  //}

  private def addOrderActor(order: Order): Unit = {
    val child = context.actorOf(Props(classOf[OrderActor], order))
    orderActors += (order.id -> child)
  }

  private def deleteOrderActor(orderId: String) = {
    orderActors(orderId) ! StopOrderActorM
    orderActors -= orderId
  }

  private def printOrderActors() = {
    orderActors.foreach(p => p._2 ! PrintOrderActorIdM)
  }


}


object OrderAllocationManagerActor {

  object messages {

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class CompletedOrderM(orderId: String)

  }

}
