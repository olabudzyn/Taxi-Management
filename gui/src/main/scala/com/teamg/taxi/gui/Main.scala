package com.teamg.taxi.gui

import java.util.concurrent.Executors

import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.teamg.taxi.core.{DefaultSimulationConfig, ServiceConfig, SimulationOrderSender, TaxiSystem}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Main extends App {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
  val simulationConfig = DefaultSimulationConfig
  val taxiSystem = new TaxiSystem(simulationConfig)

  Future(new GUI(taxiSystem, simulationConfig).main(Array.empty))

  val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  Thread.sleep(3000)
  val orderSender = new SimulationOrderSender(ServiceConfig.orderUrl)

  orderSender.send(OrderRequest("I", "S", "normal", "normal", "abc"))(ec)


}
