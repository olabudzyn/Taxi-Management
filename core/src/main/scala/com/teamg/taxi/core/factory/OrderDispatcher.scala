package com.teamg.taxi.core.factory

import com.teamg.taxi.core.model.Order

import scala.concurrent.{ExecutionContext, Future}

trait OrderDispatcher {
  def dispatch(order: Order)
              (implicit executionContext: ExecutionContext): Future[Unit]
}
