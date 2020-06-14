package com.teamg.taxi.core.map

import com.teamg.taxi.core.map.Edge.Label

object MapUtils {

  def createEdge(first: Node[String], second: Node[String], weight: Option[Double] = None): Edge[String] = {
    val edgeWeight = weight.getOrElse(LocationUtils.distance(first.location, second.location))
    Edge(Label(first.id + "-" + second.id), first, second, edgeWeight)
  }
}
