package com.teamg.taxi.integration

import java.util.concurrent.Executors

import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.teamg.taxi.core.{ServiceConfig, SimulationConfig, SimulationOrderSender, TaxiSystem}
import com.teamg.taxi.gui.GUI

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait BaseApp extends App {

  def simulationConfig: SimulationConfig

  def startGUI: Future[Unit] = Future(new GUI(taxiSystem, simulationConfig).main(Array.empty))

  def sendOrderRequest(orderRequest: OrderRequest) = orderSender.send(orderRequest)(ec)

  val taxiSystem: TaxiSystem = new TaxiSystem(simulationConfig)

  val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val orderSender = new SimulationOrderSender(ServiceConfig.orderUrl)

  private implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
}
