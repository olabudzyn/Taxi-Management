package com.teamg.taxi.core.model

sealed trait CustomerType

object CustomerType {

  case object Normal extends CustomerType

  case object Vip extends CustomerType

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

sealed trait OrderState

object OrderState {

  case class Pending(order: Order, createTime: Long) extends OrderState

  case class OnWayToCustomer(order: Order, taxiId: String) extends OrderState

  case class TransportedCustomer(order: Order, taxiId: String) extends OrderState

}

case class Order(id: String,
                 from: String,
                 target: String,
                 customerType: CustomerType,
                 orderType: OrderType)

case class Taxi(id: String,
                taxiType: TaxiType)