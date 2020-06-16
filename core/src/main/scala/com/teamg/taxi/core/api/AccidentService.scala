package com.teamg.taxi.core.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.teamg.taxi.core.api.AccidentService.AccidentRequest
import com.teamg.taxi.core.api.codecs._
import com.teamg.taxi.core.factory.AccidentsProvider
import com.teamg.taxi.core.map.CityMap
import com.teamg.taxi.core.map
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.collection.mutable.ArrayBuffer

class AccidentService(cityMap: CityMap[String])
  extends FailFastCirceSupport
    with AccidentsProvider {

  override def getAccidents: List[map.Accident[String]] = buffer.toList

  val buffer: ArrayBuffer[map.Accident[String]] = ArrayBuffer.empty

  val route: Route =
    path("accident") {
      post {
        entity(as[AccidentRequest]) { accidentRequest =>
          val center = cityMap.getCenter(accidentRequest.from, accidentRequest.to)
          center match {
            case Some(value) =>
              buffer += map.Accident(accidentRequest.from, accidentRequest.to, accidentRequest.value, value)
              complete((StatusCodes.OK, "success"))
            case None =>
              complete((StatusCodes.NotFound, "failure"))
          }
        }
      }
    }

  def getApiAccidents: List[Accident] = {
    buffer.map(acc => Accident(Location(acc.location.x, acc.location.y))).toList
  }
}

object AccidentService {

  case class AccidentRequest(from: String, to: String, value: Double)

}


