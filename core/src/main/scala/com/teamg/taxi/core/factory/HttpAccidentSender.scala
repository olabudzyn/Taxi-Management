package com.teamg.taxi.core.factory

import akka.actor.ActorSystem
import io.circe.syntax._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import com.teamg.taxi.core.api.AccidentService
import com.teamg.taxi.core.api.codecs._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpAccidentSender(url: Uri)
                        (implicit actorSystem: ActorSystem) extends AccidentSender with LazyLogging{
  override def send(accidentRequest: AccidentService.AccidentRequest)(implicit executionContext: ExecutionContext): Future[Unit] = {
    Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, accidentRequest.asJson.noSpaces)
    )).andThen {
      case Success(response) => println(s"Accident send success, statusCode:${response.status.intValue()}")
      case Failure(_) => println(s"Accident send failure")
    }.map(_ => ())
  }
}
