package com.teamg.taxi.core.api

case class Location(x: Double, y: Double)

sealed trait TaxiState

object TaxiState {

  case object Free extends TaxiState

  case object OnWayToCustomer extends TaxiState

  case object Occupied extends TaxiState

}

case class Order(id: String, location: Location)

case class Taxi(id: String, location: Location, taxiState: TaxiState)

case class TaxiSystemState(orders: List[Order], taxis: List[Taxi])
