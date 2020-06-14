package com.teamg.taxi.core.factory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.teamg.taxi.core.api.TaxiSystemState
import com.teamg.taxi.core.api.codecs._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TaxiSystemStateReceiver(url: Uri)
                             (implicit actorSystem: ActorSystem) {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  def receive(implicit executionContext: ExecutionContext): Future[TaxiSystemState] = {
    Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = url,
    )).andThen {
      case Success(response) => println(s"Status get success, statusCode:${response.status.intValue()}")
      case Failure(_) => println(s"Status send failure")
    }.flatMap(Unmarshal(_).to[TaxiSystemState])
  }
}