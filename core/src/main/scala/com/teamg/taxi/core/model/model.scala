package com.teamg.taxi.core.model

import java.time.Instant

import com.teamg.taxi.core.actors.resource.TaxiPath

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

  case class Occupied(orderId: String, taxiPath: TaxiPath) extends TaxiState

  case class OnWayToCustomer(orderId: String, taxiPath: TaxiPath) extends TaxiState

}

sealed trait OrderType

object OrderType {

  case object Normal extends OrderType

  case class Predefined(time: Long) extends OrderType

}

sealed trait OrderState

object OrderState {

  case class Pending(order: Order, createTime: Instant) extends OrderState

  case class OnWayToCustomer(order: Order, taxiId: String) extends OrderState

  case class TransportedCustomer(order: Order, taxiId: String) extends OrderState

}

case class Order(id: String,
                 from: String,
                 target: String,
                 customerType: CustomerType,
                 orderType: OrderType,
                 timeStamp: Instant)

case class Taxi(id: String,
                taxiType: TaxiType)

sealed trait TaxiPathState

case object TaxiPathState {

  case object InProgress extends TaxiPathState

  case object Finished extends TaxiPathState

}