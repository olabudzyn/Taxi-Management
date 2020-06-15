package com.teamg.taxi.integration

import com.teamg.taxi.core.api.OrderService.OrderRequest
import com.teamg.taxi.core.{DefaultSimulationConfig, ServiceConfig, SimulationOrderSender, TaxiSystem}
import com.teamg.taxi.gui.GUI
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class Test extends WordSpec
  with Matchers
  with SimulationScalaFutures {

  private val sleepValue = 100000

  val orderSender = new SimulationOrderSender(ServiceConfig.orderUrl)

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(150, Seconds), interval = Span(15, Seconds))

  "System " should {
    "receive order" when {
      "abc" in {
        implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
        val simulationConfig = DefaultSimulationConfig
        val taxiSystem = new TaxiSystem(simulationConfig)
        new GUI(taxiSystem, simulationConfig).main(Array.empty)

        val task = Future.sequence(Seq(
          orderSender.send(OrderRequest("A", "J", "normal", "normal")),
          Future(Thread.sleep(sleepValue))
        ))

        whenReady(task) { a =>

        }
      }
    }
  }


}
