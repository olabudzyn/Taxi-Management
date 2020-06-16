package com.teamg.taxi.integration

import java.util.concurrent.Executors

import com.teamg.taxi.core.api.AccidentService.AccidentRequest
import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.teamg.taxi.core.{ServiceConfig, SimulationConfig, SimulationSender, TaxiSystem}
import com.teamg.taxi.gui.GUI

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait BaseApp extends App {

  def simulationConfig: SimulationConfig

  def startGUI: Future[Unit] = Future(new GUI(taxiSystem, simulationConfig).main(Array.empty))

  def sendOrderRequest(orderRequest: OrderRequest) = simulationSender.send(orderRequest)(ec)

  def sendAccidentRequest(accidentRequest: AccidentRequest) = simulationSender.send(accidentRequest)(ec)

  val taxiSystem: TaxiSystem = new TaxiSystem(simulationConfig)

  val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val simulationSender = new SimulationSender(ServiceConfig.orderUrl, ServiceConfig.accidentUrl)

  private implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
}
