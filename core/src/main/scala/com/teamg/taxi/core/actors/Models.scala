package com.teamg.taxi.core.actors


case class Location(lat: Float, long: Float)
case class Taxi(id: Int, location: Location)

