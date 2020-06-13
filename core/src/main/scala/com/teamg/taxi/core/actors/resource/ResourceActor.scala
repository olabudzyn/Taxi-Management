package com.teamg.taxi.core.actors.resource

import java.time.Clock

import akka.actor.{Actor, ActorLogging, ActorRef}
import cats.Show
import cats.implicits._
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.TaxiUpdateResponse.{TaxiFinishedOrderM, TaxiPickUpCustomerM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, NewOrderRequestM, UpdateLocationM}
import com.teamg.taxi.core.map.{Location, MapProvider, Node}
import com.teamg.taxi.core.model.TaxiPathState.Finished
import com.teamg.taxi.core.model.{Order, Taxi, TaxiPathState, TaxiState}

class ResourceActor(clock: Clock,
                    taxi: Taxi,
                    orderAllocationManagerActor: ActorRef,
                    initialNode: Node[String]) extends Actor with ActorLogging {


  private var location = initialNode.location
  private var taxiState: TaxiState = TaxiState.Free(initialNode.id)

  private val cityMap = MapProvider.default
  private implicit val showLocation: Show[Location] = Show.show(location => s"x(${location.x.show}) y(${location.y.show})")

  override def receive: Receive = {
    case UpdateLocationM(dist) =>
      onUpdateLocation(dist)
        .map(updateResponse => orderAllocationManagerActor ! updateResponse)

    case NewOrderRequestM(order) =>
      orderAllocationManagerActor ! onNewOrderRequest(order)

    case CalculateCostM(order) =>
      taxiState match {
        case TaxiState.Free(nodeId) =>
          val cost = cityMap.minimalDistance(nodeId, order.from)
          orderAllocationManagerActor ! TaxiCostResponse(taxi, cost)
        case _: TaxiState.Occupied =>
        case _: TaxiState.OnWayToCustomer =>
      }
  }

  private def onUpdateLocation(dist: Double): Option[TaxiUpdateResponse] = {
    taxiState match {
      case _: TaxiState.Free => None
      case TaxiState.Occupied(order, taxiPath) =>
        location = taxiPath.update(location, dist)
        taxiPath.getState match {
          case Finished =>
            println(s"Course [${order.id}] finished")
            taxiState = TaxiState.Free(order.target)
            Some(TaxiFinishedOrderM(taxi, clock.instant()))
          case TaxiPathState.InProgress =>
            println(s"Course [${order.id}] in progress, location: ${location.show}")
            None
        }

      case TaxiState.OnWayToCustomer(order, taxiPath) =>
        location = taxiPath.update(location, dist)
        taxiPath.getState match {
          case Finished =>
            println(s"Taxi arrived to customer: [${order.id}]")
            val edges = cityMap.edges(order.from, order.target)
            taxiState = TaxiState.Occupied(order, TaxiPath(edges.get))
            Some(TaxiPickUpCustomerM(taxi))
          case TaxiPathState.InProgress =>
            println(s"Taxi is on the way to customer, course id: [${order.id}], location: ${location.show}")
            None
        }
    }
  }

  private def onNewOrderRequest(order: Order): TaxiOrderResponse = {
    taxiState match {
      case TaxiState.Free(nodeId) =>
        if (order.from === nodeId) {
          val startEdges = cityMap.edges(order.from, order.target)
          taxiState = TaxiState.Occupied(order, TaxiPath(startEdges.get))
          TaxiOrderResponse.TaxiPickUpCustomerM(taxi)
        } else {
          val startEdges = cityMap.edges(nodeId, order.from)
          taxiState = TaxiState.OnWayToCustomer(order, TaxiPath(startEdges.get))
          TaxiOrderResponse.TaxiOnWayToCustomerM(taxi)
        }
      case _: TaxiState.Occupied => TaxiOrderResponse.TaxiAlreadyOccupiedM(taxi)
      case _: TaxiState.OnWayToCustomer => TaxiOrderResponse.TaxiOnWayToAnotherClient(taxi)
    }
  }
}

object ResourceActor {

  object messages {

    case class UpdateLocationM(scale: Double)

    case class NewOrderRequestM(order: Order)

    case class CalculateCostM(order: Order)

  }

}
