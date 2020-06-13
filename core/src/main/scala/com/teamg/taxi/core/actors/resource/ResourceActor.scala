package com.teamg.taxi.core.actors.resource

import akka.actor.{Actor, ActorLogging, ActorRef}
import cats.Show
import cats.implicits._
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, SetTargetM, UpdateLocationM}
import com.teamg.taxi.core.map.{Location, MapProvider, Node}
import com.teamg.taxi.core.model.TaxiPathState.Finished
import com.teamg.taxi.core.model.{Order, Taxi, TaxiPathState, TaxiState}

class ResourceActor(taxi: Taxi,
                    orderAllocationManagerActor: ActorRef,
                    initialNode: Node[String]) extends Actor with ActorLogging {


  private var location = initialNode.location
  private var taxiState: TaxiState = TaxiState.Free(initialNode.id)

  private val cityMap = MapProvider.default
  private implicit val showLocation: Show[Location] = Show.show(location => s"x(${location.x.show}) y(${location.y.show})")

  override def receive: Receive = {
    case UpdateLocationM(scale) =>
      taxiState match {
        case _: TaxiState.Free =>
        case TaxiState.Occupied(order, taxiPath) =>
          location = taxiPath.update(location, scale)
          taxiPath.getState match {
            case Finished =>
              println(s"Course [${order.id}] finished")
              taxiState = TaxiState.Free(order.target)
              orderAllocationManagerActor ! TaxiFreeM(taxi)

            case TaxiPathState.InProgress => println(s"Course [${order.id}] in progress, location: ${location.show}")
              taxiState = TaxiState.Occupied(order, taxiPath)
              orderAllocationManagerActor ! TaxiOccupiedM(taxi)
          }

        case TaxiState.OnWayToCustomer(order, taxiPath) =>
          location = taxiPath.update(location, scale)
          taxiPath.getState match {
            case Finished =>
              println(s"Taxi arrived to customer: [${order.id}]")
              val edges = cityMap.edges(order.from, order.target)
              taxiState = TaxiState.Occupied(order, TaxiPath(edges.get))
              orderAllocationManagerActor ! TaxiOccupiedM(taxi)
            case TaxiPathState.InProgress => println(s"Taxi is on the way to customer, course id: [${order.id}], location: ${location.show}")
              taxiState = TaxiState.OnWayToCustomer(order, taxiPath)
              orderAllocationManagerActor ! TaxiOnWayM(taxi)
          }
      }

    case SetTargetM(order) =>
      taxiState match {
        case TaxiState.Free(nodeId) =>
          taxiState = if (order.from === nodeId) {
            val startEdges = cityMap.edges(order.from, order.target)
            orderAllocationManagerActor ! TaxiOccupiedM(taxi)
            TaxiState.Occupied(order, TaxiPath(startEdges.get))
          } else {
            val startEdges = cityMap.edges(nodeId, order.from)
            orderAllocationManagerActor ! TaxiOnWayM(taxi)
            TaxiState.OnWayToCustomer(order, TaxiPath(startEdges.get))
          }
          orderAllocationManagerActor ! TaxiAcceptedOrderM(taxi)
        case _: TaxiState.Occupied => orderAllocationManagerActor ! TaxiOccupiedM(taxi)
        case _: TaxiState.OnWayToCustomer => orderAllocationManagerActor ! TaxiOnWayM(taxi)
      }


    case CalculateCostM(order) =>
      println("Taxi is calculating cost")
      taxiState match {
        case TaxiState.Free(nodeId) =>
          if (order.from === nodeId) {
            val cost = cityMap.minimalDistance(order.from, order.target)
            orderAllocationManagerActor ! TaxiCostResponse(taxi, cost)
          } else {
            val cost = cityMap.minimalDistance(nodeId, order.from)
            orderAllocationManagerActor ! TaxiCostResponse(taxi, cost)
          }
        case _: TaxiState.Occupied =>
        case _: TaxiState.OnWayToCustomer =>
      }
  }

  def calculateTaxiCost(order: Order) {

  }


}

object ResourceActor {

  object messages {

    case class UpdateLocationM(scale: Double)

    case class SetTargetM(order: Order)

    case class CalculateCostM(order: Order)

  }

}
