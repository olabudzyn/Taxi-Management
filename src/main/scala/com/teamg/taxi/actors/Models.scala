package com.teamg.taxi.actors


case class Location(lat: Float, long: Float)
case class Taxi(id: Int, location: Location)

