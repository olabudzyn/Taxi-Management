package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.MapUtils.createEdge

object MapProvider {
  val default: CityMap[String] = {
    val nodeLabels = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U")
    val locationList = List(Location(173, 132), Location(273, 218), Location(402, 103), Location(546, 136), Location(590, 187), Location(720, 158),
      Location(651, 353), Location(497, 270), Location(566, 454), Location(673, 596), Location(460, 740), Location(445, 538), Location(353, 580),
      Location(114, 607), Location(260, 522), Location(333, 416), Location(117, 410), Location(86, 267), Location(217, 288), Location(368, 310))
    val nodes = (nodeLabels, locationList).zipped
      .map((label, location) => new Node[String](label, location))
      .foldLeft(Map[String, Node[String]]()) { (m, s) => m + (s.id -> s) }

    val edges =
      createEdge(nodes("A"), nodes("B"), 1.0) ::
        createEdge(nodes("A"), nodes("S"), 2.0) ::
        createEdge(nodes("A"), nodes("C"), 3.0) ::
        createEdge(nodes("B"), nodes("C"), 4.0) ::
        createEdge(nodes("D"), nodes("U"), 5.0) ::
        createEdge(nodes("C"), nodes("D"), 5.0) ::
        createEdge(nodes("E"), nodes("F"), 3.0) ::
        createEdge(nodes("B"), nodes("T"), 6.0) ::
        createEdge(nodes("D"), nodes("E"), 7.0) ::
        createEdge(nodes("F"), nodes("G"), 2.0) ::
        createEdge(nodes("H"), nodes("U"), 1.0) ::
        createEdge(nodes("G"), nodes("H"), 4.0) ::
        createEdge(nodes("T"), nodes("U"), 5.0) ::
        createEdge(nodes("I"), nodes("K"), 8.0) ::
        createEdge(nodes("H"), nodes("I"), 5.0) ::
        createEdge(nodes("K"), nodes("L"), 6.0) ::
        createEdge(nodes("L"), nodes("M"), 2.0) ::
        createEdge(nodes("K"), nodes("N"), 3.0) ::
        createEdge(nodes("I"), nodes("J"), 2.0) ::
        createEdge(nodes("N"), nodes("R"), 8.0) ::
        createEdge(nodes("I"), nodes("O"), 4.0) ::
        createEdge(nodes("R"), nodes("S"), 6.0) ::
        createEdge(nodes("J"), nodes("K"), 1.0) ::
        createEdge(nodes("P"), nodes("R"), 7.0) ::
        createEdge(nodes("O"), nodes("P"), 5.0) ::
        createEdge(nodes("F"), nodes("J"), 8.0) ::
        createEdge(nodes("C"), nodes("U"), 4.0) ::
        createEdge(nodes("S"), nodes("T"), 2.0) ::
        createEdge(nodes("P"), nodes("T"), 1.0) ::
        createEdge(nodes("N"), nodes("O"), 4.0) ::
        createEdge(nodes("E"), nodes("G"), 6.0) ::
        createEdge(nodes("M"), nodes("N"), 3.0) ::
        createEdge(nodes("H"), nodes("P"), 3.0) ::
        createEdge(nodes("E"), nodes("H"), 6.0) ::
        createEdge(nodes("P"), nodes("U"), 2.0) ::
        createEdge(nodes("I"), nodes("P"), 4.0) ::
        createEdge(nodes("G"), nodes("I"), 1.0) ::
        createEdge(nodes("I"), nodes("L"), 7.0) ::
        createEdge(nodes("O"), nodes("M"), 9.0) :: Nil

    CityMap(nodes.values.toSet, edges)
  }
}
