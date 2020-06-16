package com.teamg.taxi.core.model

import java.time.Instant

import com.teamg.taxi.core.actors.resource.TaxiPath
import com.teamg.taxi.core.map.Node

sealed trait CustomerType

object CustomerType {

  case object Normal extends CustomerType

  case object Vip extends CustomerType

  case object SuperVip extends CustomerType
}

sealed trait TaxiType

object TaxiType {

  case object Van extends TaxiType

  case object Car extends TaxiType

}

sealed trait TaxiState

object TaxiState {

  case class Free(nodeId: String) extends TaxiState

  case class Occupied(order: Order, taxiPath: TaxiPath) extends TaxiState

  case class OnWayToCustomer(order: Order, taxiPath: TaxiPath) extends TaxiState

}

sealed trait TaxiPureState

object TaxiPureState {

  case class Free(fromTimestamp: Instant) extends TaxiPureState

  case object Occupied extends TaxiPureState

  case object OnWayToCustomer extends TaxiPureState

}

sealed trait OrderType

object OrderType {

  case object Normal extends OrderType

  case class Predefined(time: Instant) extends OrderType

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
                 taxiType: TaxiType,
                 customerType: CustomerType,
                 orderType: OrderType,
                 timeStamp: Instant)

case class Taxi(id: String,
                taxiType: TaxiType,
                defaultNode: Node[String])

sealed trait TaxiPathState

case object TaxiPathState {

  case object InProgress extends TaxiPathState

  case object Finished extends TaxiPathState

}
