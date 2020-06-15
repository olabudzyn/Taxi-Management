package com.teamg.taxi.gui

import java.util.concurrent.TimeUnit

import com.teamg.taxi.core.api.{Location, Taxi, TaxiState, TaxiSystemState}
import com.teamg.taxi.core.{SimulationConfig, TaxiSystem}
import com.teamg.taxi.gui.GUI._
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import scalafx.scene.text.{Text, TextBoundsType}
import scalafx.scene.{Group, Scene}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.runtime.Tuple2Zipped
import scala.util.Random

class GUI(taxiSystem: TaxiSystem,
          config: SimulationConfig)
         (implicit executionContext: ExecutionContext) extends JFXApp {

  stage = new PrimaryStage() {
    title = "Taxi System"
    width = boardWidth
    height = boardHeight
    scene = createScene()
  }

  private def createScene(): Scene = {
    new Scene {
      fill = boardColor
      val canvas = new Group

      createStaticMap(canvas)
      createLegend(canvas)

      var taxiWithLabel = initializeView(canvas, config)

      content = canvas

      val timer: AnimationTimer = AnimationTimer { now => {
        if (now - lastUpdate >= refreshTime) {
          val future = taxiSystem.receive.map(r => updateValues(r))
          val responseTaxiState = Await.result(future, Duration(5, TimeUnit.SECONDS))
          updateView(canvas, taxiWithLabel, responseTaxiState)
          taxiWithLabel = responseTaxiState
          lastUpdate = now
        }
      }
      }
      timer.start()
    }
  }

  private def initializeView(canvas: Group, config: SimulationConfig): Tuple2Zipped[Text, List[Text], Circle, List[Circle]] = {
    val initialTaxi = config.taxis
    val taxiLabels: List[Text] = initialTaxi.map(t => createText(t._1, Location(t._2.defaultNode.location.x, t._2.defaultNode.location.y))).toList
    val taxiCircles: List[Circle] = initialTaxi.map(t => drawTaxi(Taxi(t._1, Location(t._2.defaultNode.location.x, t._2.defaultNode.location.y), TaxiState.Free))).toList

    taxiCircles.foreach(tc => canvas.getChildren.add(tc))
    taxiLabels.foreach(label => canvas.getChildren.add(label))

    (taxiLabels, taxiCircles).zipped
  }

  private def updateValues(responseTaxiState: TaxiSystemState): Tuple2Zipped[Text, List[Text], Circle, List[Circle]] = {
    val tempTaxis = responseTaxiState.taxis.map(t => Taxi(t.id, t.location, t.taxiState))
    val taxiLabels: List[Text] = tempTaxis.map(t => createText(t.id, t.location))
    val taxiCircles: List[Circle] = tempTaxis.map(t => drawTaxi(t))
    val taxiWithLabel = (taxiLabels, taxiCircles).zipped
    taxiWithLabel
  }

  private def updateView(canvas: Group, oldTaxiWithLabel: Tuple2Zipped[Text, List[Text], Circle, List[Circle]], taxiWithLabel: Tuple2Zipped[Text, List[Text], Circle, List[Circle]]) = {
    oldTaxiWithLabel.foreach((label, taxi) => canvas.children.removeAll(label, taxi))
    taxiWithLabel.foreach((label, taxi) => canvas.children.addAll(label, taxi))
  }

  private def createLegend(canvas: Group) = {
    canvas.getChildren.add(createText("free taxi", Location(20, 0)))
    canvas.getChildren.add(createCircle(20, 25, freeStateColor))
    canvas.getChildren.add(createText("occupied taxi", Location(120, 0)))
    canvas.getChildren.add(createCircle(120, 25, occupiedStateColor))
    canvas.getChildren.add(createText("on the way to customer taxi", Location(240, 0)))
    canvas.getChildren.add(createCircle(240, 25, onWayToCustomerStateColor))
    canvas.getChildren.add(createText("order", Location(440, 0)))
    canvas.getChildren.add(createSquare(440, 25, orderColor))

  }

  private def createText(name: String, location: Location): Text = {
    val text = new Text(name)
    text.fill = textColor
    text.relocate(location.x + padding, location.y + padding)
    text.boundsType = TextBoundsType.Visual
    text
  }

  private def createStaticMap(canvas: Group): Unit = {
    val mapElements = config.cityMap.getCityMapElements
    val circles: List[Circle] = mapElements.nodes.map(node => createCircle(node.location.x, node.location.y, mapColor))
    val paths: List[Path] = mapElements.edges.map(edge => generatePath(List(new Point2D(edge.first.location.x, edge.first.location.y),
      new Point2D(edge.second.location.x, edge.second.location.y)
    )))
    circles.foreach(circle => canvas.getChildren.add(circle))
    paths.foreach(path => canvas.getChildren.add(path))
  }

  private def createSquare(x: Double, y: Double, color: Color): Rectangle = {
    val square = new Rectangle()
    square.fill = color
    square.width = squareSize
    square.height = squareSize
    square.relocate(x - pointSize, y - pointSize)
    square
  }

  private def createCircle(x: Double, y: Double, color: Color): Circle = {
    val circle = new Circle()
    circle.fill = color
    circle.radius = pointSize
    circle.relocate(x - pointSize, y - pointSize)
    circle
  }

  private def generatePath(points: List[Point2D]): Path = {
    val path = new Path
    val first = points.head
    val listToElement: List[PathElement] = points.map(e => LineTo(e.x, e.y))
    path.elements.add(MoveTo(first.x, first.y))
    listToElement.foreach(e => path.elements.add(e))
    path.setOpacity(1)
    path
  }

  def randomState(): TaxiState = {
    val number = Random.nextInt(3)
    number match {
      case 0 => TaxiState.Free
      case 1 => TaxiState.Occupied
      case 2 => TaxiState.OnWayToCustomer
    }
  }

  def getRandomLocation: Location = {
    Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50))
  }

  private def drawTaxi(taxi: Taxi): Circle = {
    val circle = new Circle()
    circle.fill = checkState(taxi.taxiState)
    circle.radius = pointSize
    circle.relocate(taxi.location.x, taxi.location.y)
    circle
  }

  private def checkState(state: TaxiState): Color = {
    state match {
      case TaxiState.Free => freeStateColor
      case TaxiState.OnWayToCustomer => onWayToCustomerStateColor
      case TaxiState.Occupied => occupiedStateColor
    }
  }
}

object GUI {
  val pointSize = 10
  val squareSize = 2 * pointSize
  val boardWidth = 800
  val boardHeight = 800
  val refreshTime = 1000000000L
  val padding = 18
  var lastUpdate = 0L
  val freeStateColor = YellowGreen
  val occupiedStateColor = Red
  val onWayToCustomerStateColor = DarkMagenta
  val orderColor = Orange
  val boardColor = WhiteSmoke
  val textColor = Black
  val mapColor = Blue
}
