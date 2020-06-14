package com.teamg.taxi.gui

import com.teamg.taxi.core.api.{Location, Taxi, TaxiState}
import com.teamg.taxi.core.map.MapProvider
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.{Circle, LineTo, MoveTo, Path, PathElement}
import scalafx.scene.{Group, Scene}

import scala.util.Random

object GUI extends JFXApp {

  val pointSize = 10
  val boardWidth = 800
  val boardHeight = 800
  val refreshTime = 1000000000L
  private val cityMap = MapProvider.default

  stage = new PrimaryStage {
    title = "Taxi System"
    width = boardWidth
    height = boardHeight
    scene = new Scene {
      fill = WhiteSmoke
      val canvas = new Group
      createStaticMap()

      val taxis = List(Taxi("Taxi1", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free),
        Taxi("Taxi2", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free),
        Taxi("Taxi3", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free)
      )

      val taxiCircles: List[Circle] = taxis.map(t => drawTaxi(t))
      taxiCircles.foreach(tc => canvas.getChildren.add(tc))

      content = canvas

      var lastUpdate = 0L
      val timer: AnimationTimer = AnimationTimer { now => {
        if (now - lastUpdate >= refreshTime) {
          taxiCircles.foreach(tc => canvas.getChildren.remove(tc))
          taxiCircles.foreach(tc => tc.fill = checkState(randomState()))
          taxiCircles.foreach(tc => {
            val newLocation = getRandomLocation()
            tc.relocate(newLocation.x, newLocation.y)
          })
          taxiCircles.foreach(tc => canvas.getChildren.add(tc))
          lastUpdate = now
        }
      }
      }

      timer.start()

      def createStaticMap(): Unit = {
        val circles: List[Circle] = cityMap.getCityMapElements.nodes.map(node => createCircle(node.location.x, node.location.y))
        val paths: List[Path] = cityMap.getCityMapElements.edges.map(edge => generatePath(List(new Point2D(edge.first.location.x, edge.first.location.y),
          new Point2D(edge.second.location.x, edge.second.location.y))))
        circles.foreach(circle => canvas.getChildren.add(circle))
        paths.foreach(path => canvas.getChildren.add(path))
      }

      def randomState(): TaxiState = {
        val number = Random.nextInt(3)
        number match {
          case 0 => TaxiState.Free
          case 1 => TaxiState.Occupied
          case 2 => TaxiState.OnWayToCustomer
        }
      }

      def getRandomLocation(): Location = {
        Location(Random.nextInt(boardWidth-50), Random.nextInt(boardHeight-50))
      }

      def drawTaxi(taxi: Taxi): Circle = {
        val circle = new Circle()
        circle.fill = checkState(taxi.taxiState)
        circle.radius = pointSize
        circle.relocate(taxi.location.x, taxi.location.y)
        circle
      }

      def checkState(state: TaxiState): Color = {
        state match {
          case TaxiState.Free => Green
          case TaxiState.OnWayToCustomer => DarkMagenta
          case TaxiState.Occupied => Red
        }
      }

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
