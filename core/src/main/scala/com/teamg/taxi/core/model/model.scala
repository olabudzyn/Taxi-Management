package com.teamg.taxi.core.model

sealed trait ClientType

object ClientType {

  case object Normal extends ClientType

  case object Vip extends ClientType

}

sealed trait TaxiType

object TaxiType {

  case object Van extends TaxiType

  case object Car extends TaxiType

}

sealed trait TaxiState

object TaxiState {

  case object Free extends TaxiState

  case object Occupied extends TaxiState

  case object OnWayToCustomer extends TaxiState

}

sealed trait OrderType

object OrderType {
  case object Normal extends OrderType
  case class Predefined(time: Long) extends OrderType
}

case class Order(id: String,
                 from: String,
                 target: String,
                 clientType: ClientType,
                 orderType: OrderType)

case class Taxi(id: String,
                taxiType: TaxiType)