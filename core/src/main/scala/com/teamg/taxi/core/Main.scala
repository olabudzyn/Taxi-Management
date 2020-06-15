package com.teamg.taxi.core

import com.teamg.taxi.core.model.TaxiType

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {

  case class TaxiData(nodeId: String, taxiType: TaxiType)

  val simulationConfig = DefaultSimulationConfig
  val taxiSystem = new TaxiSystem(simulationConfig)

  Thread.sleep(5000)
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  val response = taxiSystem.receive
  Thread.sleep(100000)
}
