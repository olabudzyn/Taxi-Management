package com.teamg.taxi.core.actors

import java.time.{Clock, Instant, ZoneId}

import akka.actor.{Actor, ActorRef, Props}
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, SetTargetM}
import com.teamg.taxi.core.model.{Order, Taxi, TaxiPureState}
import com.teamg.taxi.core.utils.Utils

import scala.util.Random

class OrderAllocationManagerActor extends Actor {

  private var taxiActors = Map.empty[String, ActorRef]
  private var taxiStates = Map.empty[String, TaxiPureState]
  private var taxiCost = Map.empty[String, Option[Double]]
  private var taxiLastOrderTime = Map.empty[String, Instant]

  private var orderActors = Map.empty[String, ActorRef]
  val clock: Clock = Clock.system(ZoneId.of("Europe/Warsaw"))


  override def receive: Receive = {
    case ArrivedOrderM(order: Order) =>
      addOrderActor(order)
      for {
        taxi <- taxiActors.get( Utils.getRandomElement(taxiActors.keys.toSeq, new Random())) // TODO chose proper taxi
      } yield sendOrderToTaxi(order, taxi)
      printOrderActors()

    case SendTaxis(taxis) =>
      taxiActors = taxis
      taxiStates = createInitialTaxiStates(taxiActors)
      taxiLastOrderTime = createInitialTaxiLastOrderTime(taxiActors)
      taxiCost = createInitialTaxiCost(taxiActors)

    case TaxiCostResponse(taxi, cost) =>
      println(s"Taxi ${taxi.id} cost: ${cost}")
      taxiCost = taxiCost.updated(taxi.id, cost)
      println(taxiCost)

    case CompletedOrderM(orderId: String) =>
      deleteOrderActor(orderId)
      printOrderActors()

    case DispatchOrderToTaxiM(order) =>
      taxiStates = createInitialTaxiStates(taxiActors)
      taxiLastOrderTime = createInitialTaxiLastOrderTime(taxiActors)
      taxiCost = createInitialTaxiCost(taxiActors)
      getCostFromTaxis(order, taxiActors, taxiStates)
      calculateCostFunction(order, taxiCost, taxiLastOrderTime)
      for {
        taxi <- taxiActors.get("Taxi1") // TODO chose proper taxi
      } yield sendOrderToTaxi(order, taxi)


    // setting appropriate taxi state if it changes
    case response: TaxiOrderResponse =>
      response match {
        case messages.TaxiAcceptedOrderM(taxi) =>
          println(s"Taxi ${taxi.id} received order")

        case messages.TaxiOccupiedM(taxi) =>
          println(s"Taxi ${taxi.id} occupied")
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Occupied)

        case messages.TaxiOnWayM(taxi) =>
          println(s"Taxi ${taxi.id} on way to client")
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.OnWayToCustomer)

        case messages.TaxiFreeM(taxi) =>
          println(s"Taxi ${taxi.id} is free")
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Free)
          taxiLastOrderTime = taxiLastOrderTime.updated(taxi.id, clock.instant())
      }

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


  private def createInitialTaxiStates(taxiActors: Map[String, ActorRef]): Map[String, TaxiPureState] = {
    taxiActors.map(p =>
      p._1 -> TaxiPureState.Free
    ).toMap
  }

  private def createInitialTaxiCost(taxiActors: Map[String, ActorRef]): Map[String, Option[Double]] = {
    taxiActors.map(p =>
      p._1 -> None
    ).toMap
  }

  private def createInitialTaxiLastOrderTime(taxiActors: Map[String, ActorRef]): Map[String, Instant] = {
    taxiActors.map(p =>
      p._1 -> Instant.ofEpochMilli(1)
    ).toMap
  }

  private def getCostFromTaxis(order: Order, taxiActors: Map[String, ActorRef], taxiStates: Map[String, TaxiPureState]) = {
    taxiStates.foreach(p => {
      if (p._2 == TaxiPureState.Free) {
        taxiActors(p._1) ! CalculateCostM(order)
      }
    })
  }

  private def calculateCostFunction(order: Order, taxiCost: Map[String, Option[Double]], taxiLastOrderTime: Map[String, Instant]) = {
    println("Calculating cost function")
  }


}


object OrderAllocationManagerActor {

  object messages {

    sealed trait TaxiOrderResponse

    case class TaxiAcceptedOrderM(taxi: Taxi) extends TaxiOrderResponse

    case class TaxiOccupiedM(taxi: Taxi) extends TaxiOrderResponse

    case class TaxiOnWayM(taxi: Taxi) extends TaxiOrderResponse

    case class TaxiFreeM(taxi: Taxi) extends TaxiOrderResponse

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class CompletedOrderM(orderId: String)

    case class DispatchOrderToTaxiM(order: Order)

    case class SendTaxis(taxiActors: Map[String, ActorRef])

    case class TaxiCostResponse(taxi: Taxi, cost: Option[Double])

  }

}
