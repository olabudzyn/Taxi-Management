package com.teamg.taxi.core.pureactors

import akka.actor.{Actor, ActorLogging}
import cats.implicits._
import com.teamg.taxi.core.map.{Edge, Location}
import com.teamg.taxi.core.model.{Taxi, TaxiState}
import com.teamg.taxi.core.pureactors.LocationUtils._
import com.teamg.taxi.core.pureactors.ResourceActor.messages.{SetTargetM, UpdateLocationM}

class ResourceActor(taxi: Taxi,
                    private var location: Location) extends Actor with ActorLogging {
  var taxiState: TaxiState = TaxiState.Free

  private var currentEdgeIdx: Int = 0
  private var targetEdges: List[Edge[String]] = List.empty

  override def receive: Receive = {
    case UpdateLocationM(scale) =>
      taxiState match {
        case TaxiState.Free =>
        case TaxiState.Occupied =>
          println("Taxi update")
          updateLocationByDistance(scale)
        case TaxiState.OnWayToCustomer =>
      }

    case SetTargetM(edges) =>
      taxiState = TaxiState.Occupied
      targetEdges = edges
      currentEdgeIdx = 0

  }

  @scala.annotation.tailrec
  private def updateLocationByDistance(dist: Double): Unit = {
    val distanceToCurrent = distance(location, currentTargetLocation)
    val difference = distanceToCurrent - dist

    if (difference <= 0) {
      println("Changed current target")
      location = currentTargetLocation
      currentEdgeIdx += 1
      if (currentEdgeIdx === targetEdges.size) {
        // end of course
        taxiState = TaxiState.Free
        targetEdges = List.empty
        currentEdgeIdx = 0
        println("End of order")
      } else {
        updateLocationByDistance(difference)
      }
    }
    else {
      location = updateLocation(location, currentTargetLocation, dist)
    }
  }

  private def currentTargetLocation = targetEdges(currentEdgeIdx).second.location

}

object ResourceActor {

  object messages {

    case class UpdateLocationM(scale: Double)

    case class SetTargetM(edges: List[Edge[String]])

  }

}
