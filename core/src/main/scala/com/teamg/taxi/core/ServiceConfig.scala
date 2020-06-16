package com.teamg.taxi.core

object ServiceConfig {
  val serverPort: Int = 8080
  val orderUrl = s"http://localhost:$serverPort/order"
  val stateUrl = s"http://localhost:$serverPort/state"
  val accidentUrl = s"http://localhost:$serverPort/accident"
}
