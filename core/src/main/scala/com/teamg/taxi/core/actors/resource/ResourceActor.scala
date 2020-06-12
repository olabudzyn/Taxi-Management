package com.teamg.taxi.core.actors.resource

import akka.actor.{Actor, ActorLogging, ActorRef}
import cats.Show
import cats.implicits._
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{TaxiAcceptedOrderM, TaxiOccupiedM, TaxiOnWayM}
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{SetTargetM, UpdateLocationM}
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
            case TaxiPathState.InProgress => println(s"Course [${order.id}] in progress, location: ${location.show}")
              taxiState = TaxiState.Occupied(order, taxiPath)
          }

        case TaxiState.OnWayToCustomer(order, taxiPath) =>
          location = taxiPath.update(location, scale)
          taxiPath.getState match {
            case Finished =>
              println(s"Taxi arrived to customer: [${order.id}]")
              val edges = cityMap.edges(order.from, order.target)
              taxiState = TaxiState.Occupied(order, TaxiPath(edges.get))
            case TaxiPathState.InProgress => println(s"Taxi is on the way to customer, course id: [${order.id}], location: ${location.show}")
              taxiState = TaxiState.OnWayToCustomer(order, taxiPath)
          }
      }

    case SetTargetM(order) =>
      taxiState match {
        case TaxiState.Free(nodeId) =>
          val startEdges = cityMap.edges(nodeId, order.from)
          taxiState = TaxiState.OnWayToCustomer(order, TaxiPath(startEdges.get))
          orderAllocationManagerActor ! TaxiAcceptedOrderM
        case _: TaxiState.Occupied => orderAllocationManagerActor ! TaxiOccupiedM
        case _: TaxiState.OnWayToCustomer => orderAllocationManagerActor ! TaxiOnWayM
      }

  }
}

object ResourceActor {

  object messages {

    case class UpdateLocationM(scale: Double)

    case class SetTargetM(order: Order)

  }

}
