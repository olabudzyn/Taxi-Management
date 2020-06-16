package com.teamg.taxi.core.api

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.teamg.taxi.core.factory.{OrderDispatcher, OrderFactory}
import com.teamg.taxi.core.model
import com.teamg.taxi.core.model.{CustomerType, OrderType, TaxiType}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, Encoder}

import scala.concurrent.ExecutionContext

class OrderService(orderDispatcher: OrderDispatcher)
                  (implicit executionContext: ExecutionContext) extends FailFastCirceSupport {

  val route: Route =
    path("order") {
      post {
        entity(as[OrderRequest]) { orderRequest =>
          onComplete(orderDispatcher.dispatch(toOrder(orderRequest))) { _ =>
            complete("order created")
          }
        }
      }
    }

  private def toOrder(orderRequest: OrderRequest): model.Order = {
    val clientType = orderRequest.customerType match {
      case "vip" => CustomerType.Vip
      case "supervip" => CustomerType.SuperVip
      case _ => CustomerType.Normal
    }

    val orderType = orderRequest.orderType match {
      case "normal" => OrderType.Normal
      case "predefined" => OrderType.Predefined(orderRequest.time.get)
    }

    val taxiType = orderRequest.taxiType match {
      case "van" => TaxiType.Van
      case _ => TaxiType.Car
    }

    OrderFactory.create(orderRequest.from, orderRequest.to, taxiType, clientType, orderType)
  }
}

object OrderService {

  private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
      .withZone(ZoneId.systemDefault())

  case class OrderRequest(from: String,
                          to: String,
                          customerType: String,
                          orderType: String,
                          taxiType: String,
                          time: Option[Instant] = None)

  private implicit val instantEncoder: Encoder[Instant] = Encoder.encodeString.contramap(value => formatter.format(value))
  private implicit val instantDecoder: Decoder[Instant] = Decoder.decodeString.map(value => Instant.from(formatter.parse(value)))

  implicit val orderRequestEncoder: Encoder[OrderRequest] =
    Encoder.forProduct6(keys.from, keys.to, keys.customerType, keys.orderType, keys.taxiType, keys.time)(req => (req.from, req.to, req.customerType, req.orderType, req.taxiType, req.time))

  implicit val orderRequestDecoder: Decoder[OrderRequest] =
    Decoder.forProduct6(keys.from, keys.to, keys.customerType, keys.orderType, keys.taxiType, keys.time)(OrderRequest.apply)

  object keys {
    val from = "from"
    val to = "to"
    val customerType = "customerType"
    val orderType = "orderType"
    val taxiType = "taxiType"
    val time = "time"
  }

}
