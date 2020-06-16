package com.teamg.taxi.core.factory

import com.teamg.taxi.core.api.AccidentService.AccidentRequest

import scala.concurrent.{ExecutionContext, Future}

trait AccidentSender {
  def send(accidentRequest: AccidentRequest)
          (implicit executionContext: ExecutionContext): Future[Unit]
}
