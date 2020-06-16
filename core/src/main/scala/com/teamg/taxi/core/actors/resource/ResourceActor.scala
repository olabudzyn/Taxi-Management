package com.teamg.taxi.core.actors.resource

import java.time.Clock

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.teamg.taxi.core.factory.AccidentsProvider
//import cats.Show
import cats.implicits._
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.TaxiUpdateResponse.{TaxiFinishedOrderM, TaxiPickUpCustomerM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.TaxiStateM
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, GetTaxiStateM, NewOrderRequestM, UpdateLocationM}
import com.teamg.taxi.core.map.CityMap
import com.teamg.taxi.core.model.TaxiPathState.Finished
import com.teamg.taxi.core.model.{Order, Taxi, TaxiPathState, TaxiState}

class ResourceActor(clock: Clock,
                    taxi: Taxi,
                    orderAllocationManagerActor: ActorRef,
                    cityMap: CityMap[String],
                    accidentsProvider: AccidentsProvider) extends Actor with ActorLogging {
  private var location = taxi.defaultNode.location
  private var taxiState: TaxiState = TaxiState.Free(taxi.defaultNode.id)

  override def receive: Receive = {
    case GetTaxiStateM =>
      sender ! TaxiStateM(taxi.id, location, taxiState)

    case UpdateLocationM(dist) =>
      onUpdateLocation(dist)
        .map(updateResponse => orderAllocationManagerActor ! updateResponse)

    case NewOrderRequestM(order) =>
      orderAllocationManagerActor ! onNewOrderRequest(order)

    case CalculateCostM(order) =>
      val cost = taxiState match {
        case TaxiState.Free(nodeId) =>
          cityMap.minimalDistance(nodeId, order.from, accidentsProvider.getAccidents)
        case TaxiState.Occupied(actualOrder, taxiPath) => None
          Some(taxiPath.rest + cityMap.minimalDistance(actualOrder.target, order.from, accidentsProvider.getAccidents).getOrElse(0.0))
        case _: TaxiState.OnWayToCustomer => None
      }
      sender ! TaxiCostResponse(taxi, cost)
  }

  private def onUpdateLocation(dist: Double): Option[TaxiUpdateResponse] = {
    taxiState match {
      case _: TaxiState.Free => None
      case TaxiState.Occupied(order, taxiPath) =>
        location = taxiPath.update(location, dist)
        taxiPath.getState match {
          case Finished =>
            taxiState = TaxiState.Free(order.target)
            log.info(s"${taxi.id} finished course: [${order.id} (${order.customerType})]")
            Some(TaxiFinishedOrderM(taxi, clock.instant()))
          case TaxiPathState.InProgress =>
            None
        }

      case TaxiState.OnWayToCustomer(order, taxiPath) =>
        location = taxiPath.update(location, dist)
        taxiPath.getState match {
          case Finished =>
            val edges = cityMap.edges(order.from, order.target, accidentsProvider.getAccidents)
            log.info(s"${taxi.id} arrived to customer: [${order.id} (${order.customerType})], way to target: ${edges.map(_.map(_.label.value)).get}")
            taxiState = TaxiState.Occupied(order, TaxiPath(edges.get))
            Some(TaxiPickUpCustomerM(taxi, order.id))
          case TaxiPathState.InProgress =>
            None
        }
    }
  }

  private def onNewOrderRequest(order: Order): TaxiOrderResponse = {
    taxiState match {
      case TaxiState.Free(nodeId) =>
        if (order.from === nodeId) {
          val startEdges = cityMap.edges(order.from, order.target, accidentsProvider.getAccidents)
          log.info(s"${taxi.id} was in place, picked up customer: [${order.id} (${order.customerType})], way to target: ${startEdges.map(_.map(_.label.value)).get}")
          taxiState = TaxiState.Occupied(order, TaxiPath(startEdges.get))
          TaxiOrderResponse.TaxiPickUpCustomerM(taxi, order.id)
        } else {
          val startEdges = cityMap.edges(nodeId, order.from, accidentsProvider.getAccidents)
          log.info(s"${taxi.id} is on the way to customer: [${order.id} (${order.customerType})], way to customer: ${startEdges.map(_.map(_.label.value)).get}")
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

    case object GetTaxiStateM

  }

}
