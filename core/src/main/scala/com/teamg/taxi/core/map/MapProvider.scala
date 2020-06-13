package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.MapUtils.{createEdge, createNode}

object MapProvider {
  val default: CityMap[String] = {
    val nodeLabels = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U")
    val nodes = nodeLabels
      .map(createNode)
      .foldLeft(Map[String, Node[String]]()) { (m, s) => m + (s.id -> s) }

    val edges =
      createEdge(nodes("A"), nodes("B"), 1.0) ::
        createEdge(nodes("A"), nodes("E"), 2.0) ::
        createEdge(nodes("B"), nodes("D"), 3.0) ::
        createEdge(nodes("B"), nodes("C"), 4.0) ::
        createEdge(nodes("C"), nodes("E"), 5.0) ::
        createEdge(nodes("C"), nodes("D"), 5.0) ::
        createEdge(nodes("E"), nodes("F"), 3.0) ::
        createEdge(nodes("A"), nodes("D"), 6.0) ::
        createEdge(nodes("D"), nodes("E"), 7.0) ::
        createEdge(nodes("F"), nodes("G"), 2.0) ::
        createEdge(nodes("A"), nodes("J"), 1.0) ::
        createEdge(nodes("G"), nodes("H"), 4.0) ::
        createEdge(nodes("T"), nodes("U"), 5.0) ::
        createEdge(nodes("A"), nodes("M"), 8.0) ::
        createEdge(nodes("H"), nodes("I"), 5.0) ::
        createEdge(nodes("K"), nodes("L"), 6.0) ::
        createEdge(nodes("L"), nodes("M"), 2.0) ::
        createEdge(nodes("D"), nodes("K"), 3.0) ::
        createEdge(nodes("I"), nodes("J"), 2.0) ::
        createEdge(nodes("L"), nodes("P"), 8.0) ::
        createEdge(nodes("I"), nodes("T"), 4.0) ::
        createEdge(nodes("R"), nodes("S"), 6.0) ::
        createEdge(nodes("J"), nodes("K"), 1.0) ::
        createEdge(nodes("P"), nodes("R"), 7.0) ::
        createEdge(nodes("O"), nodes("P"), 5.0) ::
        createEdge(nodes("D"), nodes("P"), 8.0) ::
        createEdge(nodes("P"), nodes("U"), 4.0) ::
        createEdge(nodes("S"), nodes("T"), 2.0) ::
        createEdge(nodes("L"), nodes("O"), 1.0) ::
        createEdge(nodes("N"), nodes("O"), 4.0) ::
        createEdge(nodes("C"), nodes("R"), 6.0) ::
        createEdge(nodes("M"), nodes("N"), 3.0) ::
        createEdge(nodes("G"), nodes("O"), 3.0) ::
        createEdge(nodes("F"), nodes("M"), 9.0) :: Nil

    CityMap(nodes.values.toSet, edges)
  }
}
