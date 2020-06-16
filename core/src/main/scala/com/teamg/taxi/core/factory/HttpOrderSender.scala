package com.teamg.taxi.core.factory

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpOrderSender(url: Uri)
                     (implicit actorSystem: ActorSystem) extends OrderSender with LazyLogging{

  override def send(orderRequest: OrderRequest)
                   (implicit executionContext: ExecutionContext): Future[Unit] = {
    Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, orderRequest.asJson.noSpaces)
    )).andThen {
      case Success(response) => println(s"Order send success, statusCode:${response.status.intValue()}")
      case Failure(_) => println(s"Order send failure")
    }.map(_ => ())
  }

}
