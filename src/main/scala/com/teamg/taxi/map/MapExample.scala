package com.teamg.taxi.map

import com.teamg.taxi.map.Utils.{createEdge, createNode}

class MapExample {
  val nodeLabels = List("A", "B", "C", "D", "E")

  def run(): Unit = {
    val nodes = nodeLabels
      .map(createNode)
      .foldLeft(Map[String, Node[String]]()) { (m, s) => m + (s.id -> s) }

    val edges =
      createEdge(nodes("A"), nodes("B"), 1.0) ::
        createEdge(nodes("A"), nodes("E"), 2.0) ::
        createEdge(nodes("B"), nodes("D"), 3.0) ::
        createEdge(nodes("B"), nodes("C"), 4.0) ::
        createEdge(nodes("C"), nodes("E"), 5.0) ::
        createEdge(nodes("A"), nodes("D"), 6.0) :: Nil

    val mapProvider = MapProvider(nodes.values.toSet, edges)

    val distanceAC = mapProvider.minimalDistance("A", "C")

    distanceAC match {
      case Some(value) => println(s"Shortest distance - expected [5], actual[$value]")
      case None => println("No connection between points")
    }


  }


}
