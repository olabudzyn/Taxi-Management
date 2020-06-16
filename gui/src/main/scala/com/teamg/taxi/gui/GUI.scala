package com.teamg.taxi.gui

import java.util.concurrent.TimeUnit

import com.teamg.taxi.core.api.{Location, Order, Taxi, TaxiState}
import com.teamg.taxi.core.{SimulationConfig, TaxiSystem}
import com.teamg.taxi.gui.GUI._
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape._
import scalafx.scene.text.{Font, Text, TextBoundsType}
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
      var orderList: List[Rectangle] = List.empty

      content = canvas

      val timer: AnimationTimer = AnimationTimer { now => {
        if (now - lastUpdate >= refreshTime) {
          val future = taxiSystem.receive.map(r => (r.orders, r.taxis))
          val responseTaxiState = Await.result(future, Duration(10, TimeUnit.SECONDS))
          val newTaxiState = updateTaxiValues(responseTaxiState._2)
          val newResponseOrder = updateOrderValues(responseTaxiState._1)
          updateView(canvas, taxiWithLabel, newTaxiState, orderList, newResponseOrder)
          taxiWithLabel = newTaxiState
          orderList = newResponseOrder
          lastUpdate = now
        }
      }
      }

      timer.start()
    }
  }

  private def initializeView(canvas: Group, config: SimulationConfig): Tuple2Zipped[Text, List[Text], Circle, List[Circle]] = {
    val initialTaxi = config.taxis
    val taxiLabels: List[Text] = initialTaxi.map(t => createText(t._1, Location(t._2.defaultNode.location.x, t._2.defaultNode.location.y), padding, textColor)).toList
    val taxiCircles: List[Circle] = initialTaxi.map(t => drawTaxi(Taxi(t._1, Location(t._2.defaultNode.location.x, t._2.defaultNode.location.y), TaxiState.Free))).toList

    taxiCircles.foreach(tc => canvas.getChildren.add(tc))
    taxiLabels.foreach(label => canvas.getChildren.add(label))

    (taxiLabels, taxiCircles).zipped
  }

  private def updateTaxiValues(taxis: List[Taxi]): Tuple2Zipped[Text, List[Text], Circle, List[Circle]] = {
    val taxiLabels: List[Text] = taxis.map(t => createText(t.id, t.location, padding, textColor))
    val taxiCircles: List[Circle] = taxis.map(t => drawTaxi(t))
    val taxiWithLabel = (taxiLabels, taxiCircles).zipped
    taxiWithLabel
  }

  private def updateOrderValues(orders: List[Order]): List[Rectangle] = {
    val orderShapes = orders.map(o => createSquare(o.location.x, o.location.y, orderColor))
    orderShapes
  }

  private def updateView(canvas: Group, oldTaxiWithLabel: Tuple2Zipped[Text, List[Text], Circle, List[Circle]], taxiWithLabel: Tuple2Zipped[Text, List[Text], Circle, List[Circle]], oldOrderList: List[Rectangle], orderList: List[Rectangle]) = {
    oldTaxiWithLabel.foreach((label, taxi) => canvas.children.removeAll(label, taxi))
    taxiWithLabel.foreach((label, taxi) => canvas.children.addAll(label, taxi))
    oldOrderList.foreach(o => canvas.children.remove(o))
    orderList.foreach(o => canvas.children.add(o))
  }

  private def createLegend(canvas: Group) = {
    canvas.getChildren.add(createText("free taxi", Location(20, 0), padding, textColor))
    canvas.getChildren.add(createCircle(20, 25, freeStateColor, pointSize))
    canvas.getChildren.add(createText("occupied taxi", Location(120, 0), padding, textColor))
    canvas.getChildren.add(createCircle(120, 25, occupiedStateColor, pointSize))
    canvas.getChildren.add(createText("on the way to customer taxi", Location(240, 0), padding, textColor))
    canvas.getChildren.add(createCircle(240, 25, onWayToCustomerStateColor, pointSize))
    canvas.getChildren.add(createText("order", Location(460, 0), padding, textColor))
    canvas.getChildren.add(createSquare(460, 25, orderColor))

  }

  private def createText(name: String, location: Location, move: Int, color: Color): Text = {
    val text = new Text(name)
    text.font = Font.font(15)
    text.fill = color
    text.relocate(location.x + move, location.y + move)
    text.boundsType = TextBoundsType.Visual
    text
  }

  private def createStaticMap(canvas: Group): Unit = {
    val mapElements = config.cityMap.getCityMapElements
    val circles: List[Circle] = mapElements.nodes.map(node => createCircle(node.location.x, node.location.y, mapColor, nodeSize))
    val labels = mapElements.nodes.map(node => createText(node.id, Location(node.location.x, node.location.y), -25, Red))
    val paths: List[Path] = mapElements.edges.map(edge => generatePath(List(new Point2D(edge.first.location.x, edge.first.location.y),
      new Point2D(edge.second.location.x, edge.second.location.y)
    )))
    labels.foreach(label => canvas.getChildren.add(label))
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

  private def createCircle(x: Double, y: Double, color: Color, size: Int): Circle = {
    val circle = new Circle()
    circle.fill = color
    circle.radius = size
    circle.relocate(x - size, y - size)
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
    circle.relocate(taxi.location.x - pointSize, taxi.location.y - pointSize)
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
  val nodeSize = 15
  val squareSize = 2 * pointSize
  val boardWidth = 800
  val boardHeight = 800
  val refreshTime = 500000000L
  val padding = 12
  var lastUpdate = 0L
  val freeStateColor = YellowGreen
  val occupiedStateColor = Red
  val onWayToCustomerStateColor = DarkMagenta
  val orderColor = Orange
  val boardColor = Gray
  val textColor = Black
  val mapColor = Blue
}
