package com.teamg.taxi.core.actors.resource

import cats.implicits._
import com.teamg.taxi.core.map.{Edge, Location, LocationUtils}
import com.teamg.taxi.core.model.TaxiPathState


class TaxiPath(edges: List[Edge[String]]) {
  private var currentEdgeIdx: Int = 0
  private var counter = 0
  private val allLength = edges.map(_.weight).sum
  var rest: Double = allLength

  private var taxiPathState: TaxiPathState = TaxiPathState.InProgress

  def getState: TaxiPathState = taxiPathState

  def update(location: Location, dist: Double): Location = {
    counter += 1
    rest = allLength - counter * dist
    updateLocation(location, dist)
  }

  @scala.annotation.tailrec
  private def updateLocation(location: Location, dist: Double): Location = {
    val currentTarget = currentTargetLocation()
    val distanceToCurrent = LocationUtils.distance(location, currentTarget)
    val difference = distanceToCurrent - dist
    if (difference <= 0) {
      val tempLocation = currentTargetLocation()
      currentEdgeIdx += 1
      if (currentEdgeIdx === edges.size) {
        taxiPathState = TaxiPathState.Finished
        tempLocation
      } else {
        updateLocation(tempLocation, (-1) * difference)
      }
    }
    else {
      LocationUtils.updateLocation(location, currentTarget, dist)
    }
  }

  private def currentTargetLocation() = edges(currentEdgeIdx).second.location

}

object TaxiPath {
  def apply(edges: List[Edge[String]]): TaxiPath = new TaxiPath(edges)
}
