package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorRef, Props}
import com.teamg.taxi.core.model.Order
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{ArrivedOrderM, CompletedOrderM, DispatchOrderToTaxiM, SendTaxis, TaxiReceivedOrderM}
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.SetTargetM
import com.teamg.taxi.core.map.{Edge, MapProvider}

class OrderAllocationManagerActor extends Actor {

  private var taxiActors = Map.empty[String, ActorRef]
  private val cityMap = MapProvider.default
  private var orderActors = Map.empty[String, ActorRef]


  override def receive: Receive = {

    case ArrivedOrderM(order: Order) =>
      addOrderActor(order)
      printOrderActors()

    case CompletedOrderM(orderId: String) =>
      deleteOrderActor(orderId)
      printOrderActors()

    case DispatchOrderToTaxiM(order) =>
      for {
        edges <- cityMap.edges(order.from, order.target)
        taxi <- taxiActors.get("1")   // TODO chose proper taxi
      } yield sendOrderToTaxi(order.id, edges, taxi)


    case TaxiReceivedOrderM =>
      println(s"Taxi ${sender().path} received order")

    case SendTaxis(taxis) =>
      taxiActors = taxis
  }


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

  def sendOrderToTaxi(orderId: String, edges: List[Edge[String]], taxi: ActorRef): Unit = {
    println("send order to taxi")
    taxi ! SetTargetM(orderId, edges)
  }

}


object OrderAllocationManagerActor {

  object messages {

    case object TaxiReceivedOrderM

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class CompletedOrderM(orderId: String)

    case class DispatchOrderToTaxiM(order: Order)

    case class SendTaxis(taxiActors: Map[String, ActorRef])

  }

}
