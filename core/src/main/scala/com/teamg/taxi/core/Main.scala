package com.teamg.taxi.core

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {

  val taxiSystem = new TaxiSystem()
  Thread.sleep(5000)
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  val response = taxiSystem.receive

  Thread.sleep(100000)

}
