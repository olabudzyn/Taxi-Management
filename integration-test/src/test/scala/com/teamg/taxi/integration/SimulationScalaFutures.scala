package com.teamg.taxi.integration

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

trait SimulationScalaFutures extends ScalaFutures{
  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(150, Seconds), interval = Span(15, Seconds))
}
