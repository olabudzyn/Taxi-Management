package com.teamg.taxi.gui

import com.teamg.taxi.core.map.MapProvider
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import scalafx.scene.{Group, Scene}

object GUI extends JFXApp {

  val pointSize = 10
  val boardWidth = 800
  val boardHeight = 800
  private val cityMap = MapProvider.default

  stage = new PrimaryStage {
    title = "Taxi System"
    width = boardWidth
    height = boardHeight
    scene = new Scene {
      fill = WhiteSmoke
      val canvas = new Group()

      val circles: List[Circle] = cityMap.getCityMapElements.nodes.map(node =>  createCircle(node.location.x, node.location.y)).toList
      val paths: List[Path] = cityMap.getCityMapElements.edges.map(edge => generatePath(List(new Point2D(edge.first.location.x, edge.first.location.y),
        new Point2D(edge.second.location.x, edge.second.location.y))))

      circles.foreach(circle => canvas.getChildren.add(circle))
      paths.foreach(path => canvas.getChildren.add(path))
      content = canvas

      def createCircle(x: Double, y: Double): Circle = {
        val circle = new Circle()
        circle.fill = Blue
        circle.radius = pointSize
        circle.relocate(x - pointSize, y - pointSize)
        circle
      }

      def generatePath(points: List[Point2D]): Path = {
        val path = new Path
        val first = points.head
        val listToElement: List[PathElement] = points.map(e => LineTo(e.x, e.y))
        path.elements.add(MoveTo(first.x, first.y))
        listToElement.foreach(e => path.elements.add(e))
        path.setOpacity(1)
        path
      }
    }
  }
}
