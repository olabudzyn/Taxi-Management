package com.teamg.taxi.core.factory

import com.teamg.taxi.core.api.TaxiSystemState

import scala.concurrent.{ExecutionContext, Future}

trait TaxiSystemStateFetcher {

  def getTaxiSystemState(implicit executionContext: ExecutionContext): Future[TaxiSystemState]

}
