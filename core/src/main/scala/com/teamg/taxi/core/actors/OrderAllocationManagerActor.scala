package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorRef, Props}
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.SetTargetM
import com.teamg.taxi.core.model.Order

class OrderAllocationManagerActor extends Actor {

  private var taxiActors = Map.empty[String, ActorRef]
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
        taxi <- taxiActors.get("1") // TODO chose proper taxi
      } yield sendOrderToTaxi(order, taxi)

    case response: TaxiOrderResponse =>
      response match {
        case messages.TaxiAcceptedOrderM => println(s"Taxi received order")
        case messages.TaxiOccupiedM => println(s"Taxi occupied")
        case messages.TaxiOnWayM => println(s"Taxi on way to client")
      }

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

  def sendOrderToTaxi(order: Order, taxi: ActorRef): Unit = {
    println("send order to taxi")
    taxi ! SetTargetM(order)
  }

}


object OrderAllocationManagerActor {

  object messages {

    sealed trait TaxiOrderResponse

    case object TaxiAcceptedOrderM extends TaxiOrderResponse

    case object TaxiOccupiedM extends TaxiOrderResponse

    case object TaxiOnWayM extends TaxiOrderResponse

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class CompletedOrderM(orderId: String)

    case class DispatchOrderToTaxiM(order: Order)

    case class SendTaxis(taxiActors: Map[String, ActorRef])

  }

}
