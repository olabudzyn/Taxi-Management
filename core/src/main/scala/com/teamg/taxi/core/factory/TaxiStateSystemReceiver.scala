package com.teamg.taxi.core.factory

import com.teamg.taxi.core.api.TaxiSystemState

import scala.concurrent.{ExecutionContext, Future}

trait TaxiStateSystemReceiver {
  def receive(implicit executionContext: ExecutionContext): Future[TaxiSystemState]
}
