package com.teamg.taxi.integration

import com.teamg.taxi.core.api.OrderService.OrderRequest

object AppTestVanOrder extends BaseApp {
  override def simulationConfig = DefaultSimulationConfigVanOrder

  startGUI

  sendOrderRequest(OrderRequest("U", "C", "normal", "normal", "abc"))
  sendOrderRequest(OrderRequest("H", "F", "vip", "normal", "van"))
}
