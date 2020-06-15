package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.MapUtils.createEdge

object MapProvider {
  val default: CityMap[String] = {
    val nodesData = Map(
      "A" -> Location(173, 132),
      "B" -> Location(273, 218),
      "C" -> Location(402, 103),
      "D" -> Location(546, 136),
      "E" -> Location(590, 187),
      "F" -> Location(720, 158),
      "G" -> Location(651, 353),
      "H" -> Location(497, 270),
      "I" -> Location(566, 454),
      "J" -> Location(673, 596),
      "K" -> Location(460, 740),
      "L" -> Location(445, 538),
      "M" -> Location(353, 580),
      "N" -> Location(114, 607),
      "O" -> Location(260, 522),
      "P" -> Location(333, 416),
      "R" -> Location(117, 410),
      "S" -> Location(86, 267),
      "T" -> Location(217, 288),
      "U" -> Location(368, 310)
    )

    val nodes = nodesData
      .map(entry => entry._1 -> Node(entry._1, entry._2))

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
