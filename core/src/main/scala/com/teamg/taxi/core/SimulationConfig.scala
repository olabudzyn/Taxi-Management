package com.teamg.taxi.core

import com.teamg.taxi.core.Main.TaxiData
import com.teamg.taxi.core.map.{CityMap, MapProvider}
import com.teamg.taxi.core.model.Taxi
import com.teamg.taxi.core.model.TaxiType.{Car, Van}

trait SimulationConfig {
  def stepSize: Double

  def taxis: Map[String, Taxi]

  def cityMap: CityMap[String]
}

object DefaultSimulationConfig extends SimulationConfig {
  override def stepSize: Double = 40

  override def taxis: Map[String, Taxi] = Map(
    "Taxi1" -> TaxiData("A", Car),
    "Taxi2" -> TaxiData("B", Car),
    "Taxi3" -> TaxiData("F", Van),
    "Taxi4" -> TaxiData("E", Car),
    "Taxi5" -> TaxiData("N", Van),
    "Taxi6" -> TaxiData("M", Car),
    "Taxi7" -> TaxiData("O", Car),
    "Taxi8" -> TaxiData("U", Car),
    "Taxi9" -> TaxiData("T", Van),
    "Taxi10" -> TaxiData("C", Car),
  ).map { entry =>
    val defaultNode = cityMap.getNode(entry._2.nodeId)
    entry._1 -> Taxi(entry._1, entry._2.taxiType, defaultNode.get)
  }

  override def cityMap: CityMap[String] = MapProvider.default
}
