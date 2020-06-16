package com.teamg.taxi.core.api

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

case class Location(x: Double, y: Double)

sealed trait TaxiState

object TaxiState {

  case object Free extends TaxiState

  case object OnWayToCustomer extends TaxiState

  case object Occupied extends TaxiState

}

sealed trait TaxoType

case class Order(id: String, location: Location)

case class Taxi(id: String, location: Location, taxiState: TaxiState)

case class TaxiSystemState(orders: List[Order], taxis: List[Taxi])

object codecs {
  implicit val locationEncoder: Encoder[Location] = deriveEncoder[Location]
  implicit val orderEncoder: Encoder[Order] = deriveEncoder[Order]
  implicit val taxiStateEncoder: Encoder[TaxiState] = deriveEncoder[TaxiState]
  implicit val taxiEncoder: Encoder[Taxi] = deriveEncoder[Taxi]
  implicit val taxiSystemStateEncoder: Encoder[TaxiSystemState] = Encoder.encodeJson.contramap( state =>
    Json.obj((keys.orders, state.orders.asJson),(keys.taxis, state.taxis.asJson))
  )

  implicit val locationDecoder: Decoder[Location] = deriveDecoder[Location]
  implicit val orderDecoder: Decoder[Order] = deriveDecoder[Order]
  implicit val taxiStateDecoder: Decoder[TaxiState] = deriveDecoder[TaxiState]
  implicit val taxiDecoder: Decoder[Taxi] = deriveDecoder[Taxi]

  implicit val taxiSystemStateDecoder: Decoder[TaxiSystemState] =
    Decoder.forProduct2(keys.orders, keys.taxis)(TaxiSystemState.apply)

  object keys {
    val orders = "orders"
    val taxis = "taxis"
  }

}