package com.teamg.taxi.integration

import com.teamg.taxi.core.Main.TaxiData
import com.teamg.taxi.core.SimulationConfig
import com.teamg.taxi.core.model.Taxi
import com.teamg.taxi.core.map.{CityMap, MapProvider}
import com.teamg.taxi.core.model.TaxiType.{Car, Van}

object DefaultSimulationConfigVanOrder extends SimulationConfig {
  override def stepSize: Double = 40

  override def taxis: Map[String, Taxi] = Map(
    "Taxi1" -> TaxiData("G", Car),
    "Taxi2" -> TaxiData("J", Car),
    "Taxi3" -> TaxiData("N", Van),
    "Taxi4" -> TaxiData("I", Car),
    "Taxi5" -> TaxiData("K", Van),
    "Taxi6" -> TaxiData("M", Car),
    "Taxi7" -> TaxiData("E", Car),
    "Taxi8" -> TaxiData("P", Car),
    "Taxi9" -> TaxiData("S", Van),
    "Taxi10" -> TaxiData("F", Car),
  ).map { entry =>
    val defaultNode = cityMap.getNode(entry._2.nodeId)
    entry._1 -> Taxi(entry._1, entry._2.taxiType, defaultNode.get)
  }

  override def cityMap: CityMap[String] = MapProvider.default
}
