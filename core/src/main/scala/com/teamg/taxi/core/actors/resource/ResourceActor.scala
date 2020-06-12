package com.teamg.taxi.core.actors.resource

import akka.actor.{Actor, ActorLogging}
import cats.Show
import cats.implicits._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{SetTargetM, UpdateLocationM}
import com.teamg.taxi.core.map.{Edge, Location}
import com.teamg.taxi.core.model.TaxiPathState.Finished
import com.teamg.taxi.core.model.{Taxi, TaxiPathState, TaxiState}

class ResourceActor(taxi: Taxi,
                    private var location: Location) extends Actor with ActorLogging {
  var taxiState: TaxiState = TaxiState.Free

  private implicit val showLocation: Show[Location] = Show.show(location => s"x(${location.x.show}) y(${location.y.show})")

  override def receive: Receive = {
    case UpdateLocationM(scale) =>
      taxiState match {
        case TaxiState.Free =>
        case TaxiState.Occupied(orderId, taxiPath) =>
          this.location = taxiPath.update(this.location, scale)
          taxiPath.getState match {
            case Finished =>
              println(s"Course [$orderId] finished")
              taxiState = TaxiState.Free
            case TaxiPathState.InProgress => println(s"Course [$orderId] in progress, location: ${this.location.show}")
              taxiState = TaxiState.Occupied(orderId, taxiPath)
          }

        case TaxiState.OnWayToCustomer(orderId, taxiPath) =>

      }

    case SetTargetM(orderId, edges) =>
      taxiState = TaxiState.Occupied(orderId, TaxiPath(edges))
  }


}

object ResourceActor {

  object messages {

    case class UpdateLocationM(scale: Double)

    case class SetTargetM(orderId: String, edges: List[Edge[String]])

  }

}
