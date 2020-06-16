package com.teamg.taxi.integration

import com.teamg.taxi.core.api.OrderService.OrderRequest

object AppTestOneTaxiTwoOrders extends BaseApp {
  override def simulationConfig = DefaultSimulationConfigOneTaxiTwoOrders

  startGUI

  sendOrderRequest(OrderRequest("T", "B", "normal", "normal", "abc"))
  //sendOrderRequest(OrderRequest("A", "C", "vip", "normal", "abc"))
}
