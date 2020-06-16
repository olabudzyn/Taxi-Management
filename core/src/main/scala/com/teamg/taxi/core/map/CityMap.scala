package com.teamg.taxi.core.map

import cats.Eq
import cats.implicits._
import com.teamg.taxi.core.map.Edge.Label
import com.teamg.taxi.core.utils.Utils._
import scalax.collection.Graph
import scalax.collection.edge.WLUnDiEdge

import scala.util.Random


class CityMap[ID](graph: Graph[Node[ID], WLUnDiEdge]) {

  implicit val eqFoo: Eq[ID] = Eq.fromUniversalEquals

  def minimalDistance(fromId: ID, toId: ID): Option[Double] = {
    shortestPath(fromId, toId).map(_.weight)
  }

  def edges(fromId: ID, toId: ID): Option[List[Edge[ID]]] = {
    edgesOnPath(fromId, toId)
  }

  def getCityMapElements: CityMapElements[ID] = {
    val nodes = graph.nodes.map(n => Node(n.id, n.location)).toList
    val edges = graph.edges.map(e => Edge(Label.empty, e.head.value, e.to.value, e.weight)).toList
    CityMapElements(nodes, edges)
  }

  def getNode(id: ID): Option[Node[ID]] = {
    graph.nodes
      .find(node => node.value.id === id)
      .map(_.value)
  }

  def randomNode(): Node[ID] = {
    val nodes = graph.nodes.map(_.value).toSeq
    getRandomElement(nodes, new Random())
  }

  private def edgesOnPath(fromId: ID, toId: ID): Option[List[Edge[ID]]] = {
    val path = shortestPath(fromId, toId)
      .map {
        _.edges.map { e =>
          val from = e.head.value
          val to = e.to.value
          createEdge(from, to, e.weight)
        }.toList
      }

    val firstEdge = path.map(_.head).get
    val properFirstEdge = if (firstEdge.first.id === fromId) {
      firstEdge
    } else {
      createEdge(firstEdge.second, firstEdge.first, firstEdge.weight)
    }

    path.map { list =>
      list.drop(1).scanLeft(properFirstEdge) { (prev, edge) =>
        if (prev.second.id === edge.first.id) {
          edge
        }
        else if (prev.second.id === edge.second.id) {
          createEdge(edge.second, edge.first, edge.weight)
        } else {
          edge
        }
      }
    }
  }

  private def createEdge(first: Node[ID], second: Node[ID], weight: Double): Edge[ID] = {
    Edge(Label(s"${first.id}-${second.id}"), first, second, weight)
  }

  private def shortestPath(fromId: ID, toId: ID): Option[graph.Path] = {
    for {
      nodeFrom <- graph.nodes.find(_.id === fromId)
      nodeTo <- graph.nodes.find(_.id === toId)
      shortestPath <- nodeFrom.shortestPathTo(nodeTo)
    } yield shortestPath
  }
}

object CityMap {

  def apply[ID](nodes: Set[Node[ID]], edges: List[Edge[ID]]): CityMap[ID] = {
    val graphEdges = edges.map(graphEdge)
    val graph = Graph.from(nodes, graphEdges)
    new CityMap(graph)
  }

  private def graphEdge[ID](edge: Edge[ID]): WLUnDiEdge[Node[ID]] = {
    WLUnDiEdge(edge.first, edge.second)(edge.weight, edge.label)
  }
}
