package com.teamg.taxi.core.api
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, onComplete, path, _}
import akka.http.scaladsl.server.Route
import com.teamg.taxi.core.actors.systemwatcher.TaxiSystemStateFetcher
import com.teamg.taxi.core.api.codecs._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
class SystemService(taxiSystemStateFetcher: TaxiSystemStateFetcher)
                   (implicit executionContext: ExecutionContext) extends FailFastCirceSupport {
  val route: Route =
    path("state") {
      get {
        onComplete(taxiSystemStateFetcher.getTaxiSystemState(executionContext)) {
          case Success(systemState) => complete((StatusCodes.OK, systemState))
          case Failure(exception) =>
            println(exception.printStackTrace())
            complete("error")
        }
      }
    }
}
