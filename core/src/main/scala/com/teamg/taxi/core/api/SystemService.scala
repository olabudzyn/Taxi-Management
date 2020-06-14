package com.teamg.taxi.core.api

import akka.http.scaladsl.server.Directives.{complete, get, onComplete, path}
import akka.http.scaladsl.server.Route
import com.teamg.taxi.core.actors.systemwatcher.TaxiSystemStateFetcher
import com.teamg.taxi.core.api.codecs._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class SystemService(taxiSystemStateFetcher: TaxiSystemStateFetcher)
                   (implicit executionContext: ExecutionContext) {

  val route: Route =
    path("state") {
      get {
        onComplete(taxiSystemStateFetcher.getTaxiSystemState(executionContext)) {
          case Success(systemState) => complete(systemState.asJson.noSpaces)
          case Failure(exception) =>
            println(exception.printStackTrace())
            complete("error")
        }
      }
    }
}
