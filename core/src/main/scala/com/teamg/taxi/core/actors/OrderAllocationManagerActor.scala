package com.teamg.taxi.core.actors

import java.time.{Clock, Instant}

import akka.actor.{Actor, ActorRef, Props}
import cats.Eq
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.UnallocatedOrdersM
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, NewOrderRequestM}
import com.teamg.taxi.core.model.{Order, Taxi, TaxiPureState}
import com.teamg.taxi.core.utils.Utils

import scala.util.Random

class OrderAllocationManagerActor(clock: Clock) extends Actor {

  private var taxiActors: Map[String, ActorRef] = Map.empty
  private var taxiStates: Map[String, TaxiPureState] = Map.empty
  private var taxiCost: Map[String, Option[Double]] = Map.empty

  private var orderActors: Map[String, ActorRef] = Map.empty

  private var orders: Map[String, Order] = Map.empty

  override def receive: Receive = {
    case ArrivedOrderM(order: Order) =>
      getCostFromTaxis(order, taxiActors, taxiStates)
      taxiStates = createInitialTaxiStates(taxiActors)
      taxiCost = createInitialTaxiCost(taxiActors)
      calculateCostFunction(order, taxiCost, taxiStates)
      addOrderActor(order)
      orders += order.id -> order
      for {
        taxi <- taxiActors.get(Utils.getRandomElement(taxiActors.keys.toSeq, new Random())) // TODO chose proper taxi
      } yield sendOrderToTaxi(order, taxi)

    case SendTaxis(taxis) =>
      taxiActors = taxis
      taxiStates = createInitialTaxiStates(taxiActors)
      taxiCost = createInitialTaxiCost(taxiActors)

    case TaxiCostResponse(taxi, cost) =>
      println(s"${taxi.id} cost: ${cost}")
      taxiCost = taxiCost.updated(taxi.id, cost)

    case response: TaxiUpdateResponse =>
      response match {
        case TaxiUpdateResponse.TaxiFinishedOrderM(taxi, timestamp) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Free(timestamp))
        case TaxiUpdateResponse.TaxiPickUpCustomerM(taxi) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Occupied)
      }

    case response: TaxiOrderResponse =>
      response match {
        case TaxiOrderResponse.TaxiOnWayToCustomerM(taxi) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.OnWayToCustomer)
        case TaxiOrderResponse.TaxiPickUpCustomerM(taxi) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Occupied)
        case TaxiOrderResponse.TaxiAlreadyOccupiedM(taxi) =>
        case TaxiOrderResponse.TaxiOnWayToAnotherClient(taxi) =>
      }

    case GetUnallocatedOrdersM=>
      sender ! UnallocatedOrdersM(orders.values.toList)

  }

  private def addOrderActor(order: Order): Unit = {
    val child = context.actorOf(Props(classOf[OrderActor], order))
    orderActors += (order.id -> child)
  }

  def deleteOrderActor(orderId: String) = {
    orderActors(orderId) ! StopOrderActorM
    orderActors -= orderId
  }

  def printOrderActors() = {
    orderActors.foreach(p => p._2 ! PrintOrderActorIdM)
  }

  def sendOrderToTaxi(order: Order, taxi: ActorRef): Unit = {
    println("send order to taxi")
    taxi ! NewOrderRequestM(order)
  }


  private def createInitialTaxiStates(taxiActors: Map[String, ActorRef]): Map[String, TaxiPureState] = {
    val timestamp = clock.instant()
    taxiActors.map(p =>
      p._1 -> TaxiPureState.Free(timestamp)
    )
  }

  private def createInitialTaxiCost(taxiActors: Map[String, ActorRef]): Map[String, Option[Double]] = {
    taxiActors.map(p =>
      p._1 -> None
    ).toMap
  }

  private def getCostFromTaxis(order: Order, taxiActors: Map[String, ActorRef], taxiStates: Map[String, TaxiPureState]) = {
    taxiStates
      .filter(p => p._2 match {
        case _: TaxiPureState.Free => true
        case _ => false
      })
      .map(p => taxiActors(p._1) ! CalculateCostM(order))
  }

  private def calculateCostFunction(order: Order, taxiCost: Map[String, Option[Double]], taxiStates: Map[String, TaxiPureState]) = {
    println("Calculating cost function")
  }

  implicit val eqFoo: Eq[TaxiPureState] = Eq.fromUniversalEquals

}


object OrderAllocationManagerActor {

  object messages {

    sealed trait TaxiOrderResponse

    object TaxiOrderResponse {

      case class TaxiAlreadyOccupiedM(taxi: Taxi) extends TaxiOrderResponse

      case class TaxiPickUpCustomerM(taxi: Taxi) extends TaxiOrderResponse

      case class TaxiOnWayToCustomerM(taxi: Taxi) extends TaxiOrderResponse

      case class TaxiOnWayToAnotherClient(taxi: Taxi) extends TaxiOrderResponse

    }

    sealed trait TaxiUpdateResponse

    object TaxiUpdateResponse {

      case class TaxiFinishedOrderM(taxi: Taxi, orderCompletionTimestamp: Instant) extends TaxiUpdateResponse

      case class TaxiPickUpCustomerM(taxi: Taxi) extends TaxiUpdateResponse

    }

    case object GetUnallocatedOrdersM

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class DispatchOrderToTaxiM(order: Order)

    case class SendTaxis(taxiActors: Map[String, ActorRef])

    case class TaxiCostResponse(taxi: Taxi, cost: Option[Double])

  }

}
