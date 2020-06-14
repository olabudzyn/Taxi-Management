package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.Edge.Label

object MapUtils {

  def createEdge(first: Node[String], second: Node[String], weight: Double): Edge[String] = {
    Edge(Label(first.id + "-" + second.id), first, second, weight)
  }
}
