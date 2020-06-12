package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.Edge.Label


case class Node[ID](id: ID, location: Location)

case class Edge[ID](label: Label, first: Node[ID], second: Node[ID], weight: Double)

case class Location(x: Double, y: Double)

object Edge {

  case class Label(value: String) extends AnyVal

  val empty: Label = Label("")

}



