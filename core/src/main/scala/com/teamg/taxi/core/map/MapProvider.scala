package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.Utils.{createEdge, createNode}

object MapProvider {
  val default: CityMap[String] = {
    val nodeLabels = List("A", "B", "C", "D", "E")
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

    CityMap(nodes.values.toSet, edges)
  }
}
