package com.teamg.taxi.integration

import com.teamg.taxi.core.DefaultSimulationConfig
import com.teamg.taxi.core.api.OrderService.OrderRequest

object AppTest extends BaseApp {
  override def simulationConfig = DefaultSimulationConfig

  startGUI

  sendOrderRequest(OrderRequest("I", "S", "normal", "normal", "abc"))
  sendOrderRequest(OrderRequest("I", "S", "normal", "normal", "abc"))
  sendOrderRequest(OrderRequest("I", "S", "normal", "normal", "abc"))
}
