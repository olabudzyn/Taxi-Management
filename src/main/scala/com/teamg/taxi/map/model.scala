package com.teamg.taxi.map

import com.teamg.taxi.map.Edge.Label


case class Node[ID](id: ID, x: Double, y: Double)

case class Edge[ID](label: Label, first: Node[ID], second: Node[ID], weight: Double)

object Edge {

  case class Label(value: String) extends AnyVal

}
