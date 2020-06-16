package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.Edge.Label


case class Node[ID](id: ID, location: Location)

case class Edge[ID](label: Label, first: Node[ID], second: Node[ID], weight: Double)

case class CityMapElements[ID](nodes: List[Node[ID]], edges: List[Edge[ID]])

case class Location(x: Double, y: Double)

object Edge {

  case class Label(value: String) extends AnyVal

  object Label {
    val empty: Label = Label("")
  }

}

case class Accident[ID](from: ID, to: ID, weight: Double, location: Location)

