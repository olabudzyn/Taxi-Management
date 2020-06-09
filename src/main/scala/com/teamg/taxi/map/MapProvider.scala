package com.teamg.taxi.map

import cats.Eq
import cats.implicits._
import scalax.collection.Graph
import scalax.collection.edge.WLUnDiEdge


class MapProvider[ID](graph: Graph[Node[ID], WLUnDiEdge]) {
  implicit val eqFoo: Eq[ID] = Eq.fromUniversalEquals

  def minimalDistance(fromId: ID, toId: ID): Option[Double] = {
    shortestPath(fromId, toId).map(_.weight)
  }

  private def shortestPath(fromId: ID, toId: ID): Option[graph.Path] = {
    for {
      nodeFrom <- graph.nodes.find(_.id === fromId)
      nodeTo <- graph.nodes.find(_.id === toId)
      shortestPath <- nodeFrom.shortestPathTo(nodeTo)
    } yield shortestPath
  }
}

object MapProvider {

  def apply[ID](nodes: Set[Node[ID]], edges: List[Edge[ID]]): MapProvider[ID] = {
    val graphEdges = edges.map(graphEdge)
    val graph = Graph.from(nodes, graphEdges)
    new MapProvider(graph)
  }

  private def graphEdge[ID](edge: Edge[ID]): WLUnDiEdge[Node[ID]] = {
    WLUnDiEdge(edge.first, edge.second)(edge.weight, edge.label)
  }
}
