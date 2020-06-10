package com.teamg.taxi.core.actors

case object NewOrder
case object Order
case object Disposition
case object LocationReport

case class SetNewLocation(location: Location)
case class TaxiLocationReport(taxi: Taxi)
case class NewTaxi(taxi: Taxi)
case class GetLocation(taxi: Taxi)
