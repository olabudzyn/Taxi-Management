package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.MapUtils.createEdge

object MapProvider {
  val default: CityMap[String] = {
    val nodeLabels = List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U")
    val locationList = List(Location(173, 132), Location(273, 218), Location(402, 103), Location(546, 136), Location(590, 187), Location(720, 158),
      Location(651, 353), Location(497, 270), Location(566, 454), Location(673, 596), Location(460, 740), Location(445, 538), Location(353, 580),
      Location(114, 607), Location(260, 522), Location(333, 416), Location(117, 410), Location(86, 267), Location(217, 288), Location(368, 310))
    val nodes = (nodeLabels, locationList).zipped
      .map((label, location) => Node(label, location))
      .foldLeft(Map[String, Node[String]]()) { (m, s) => m + (s.id -> s) }

    val edges =
      createEdge(nodes("A"), nodes("B")) ::
        createEdge(nodes("A"), nodes("S")) ::
        createEdge(nodes("A"), nodes("C")) ::
        createEdge(nodes("B"), nodes("C")) ::
        createEdge(nodes("D"), nodes("U")) ::
        createEdge(nodes("C"), nodes("D")) ::
        createEdge(nodes("E"), nodes("F")) ::
        createEdge(nodes("B"), nodes("T")) ::
        createEdge(nodes("D"), nodes("E")) ::
        createEdge(nodes("F"), nodes("G")) ::
        createEdge(nodes("H"), nodes("U")) ::
        createEdge(nodes("G"), nodes("H")) ::
        createEdge(nodes("T"), nodes("U")) ::
        createEdge(nodes("I"), nodes("K")) ::
        createEdge(nodes("H"), nodes("I")) ::
        createEdge(nodes("K"), nodes("L")) ::
        createEdge(nodes("L"), nodes("M")) ::
        createEdge(nodes("K"), nodes("N")) ::
        createEdge(nodes("I"), nodes("J")) ::
        createEdge(nodes("N"), nodes("R")) ::
        createEdge(nodes("I"), nodes("O")) ::
        createEdge(nodes("R"), nodes("S")) ::
        createEdge(nodes("J"), nodes("K")) ::
        createEdge(nodes("P"), nodes("R")) ::
        createEdge(nodes("O"), nodes("P")) ::
        createEdge(nodes("F"), nodes("J")) ::
        createEdge(nodes("C"), nodes("U")) ::
        createEdge(nodes("S"), nodes("T")) ::
        createEdge(nodes("P"), nodes("T")) ::
        createEdge(nodes("N"), nodes("O")) ::
        createEdge(nodes("E"), nodes("G")) ::
        createEdge(nodes("M"), nodes("N")) ::
        createEdge(nodes("H"), nodes("P")) ::
        createEdge(nodes("E"), nodes("H")) ::
        createEdge(nodes("P"), nodes("U")) ::
        createEdge(nodes("I"), nodes("P")) ::
        createEdge(nodes("G"), nodes("I")) ::
        createEdge(nodes("I"), nodes("L")) ::
        createEdge(nodes("O"), nodes("M")) :: Nil

    CityMap(nodes.values.toSet, edges)
  }
}
