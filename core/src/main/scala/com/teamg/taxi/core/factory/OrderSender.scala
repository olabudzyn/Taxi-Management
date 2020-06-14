package com.teamg.taxi.core.factory

import com.teamg.taxi.core.api.OrderService.OrderRequest

import scala.concurrent.{ExecutionContext, Future}

trait OrderSender {
  def send(orderRequest: OrderRequest)
          (implicit executionContext: ExecutionContext): Future[Unit]
}
