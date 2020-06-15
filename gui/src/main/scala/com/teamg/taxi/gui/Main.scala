package com.teamg.taxi.gui

import com.teamg.taxi.core.{DefaultSimulationConfig, TaxiSystem}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  val simulationConfig = DefaultSimulationConfig
  val taxiSystem = new TaxiSystem(simulationConfig)

  new GUI(taxiSystem, simulationConfig).main(Array.empty)
}
