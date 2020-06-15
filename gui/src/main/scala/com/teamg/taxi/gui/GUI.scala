package com.teamg.taxi.gui

import java.util.concurrent.TimeUnit

import com.teamg.taxi.core.TaxiSystem
import com.teamg.taxi.core.api.{Location, Taxi, TaxiState, TaxiSystemState}
import com.teamg.taxi.core.map.MapProvider
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
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Random, Success}
import com.teamg.taxi.gui.GUI._

class GUI extends JFXApp {

  private val cityMap = MapProvider.default
  val taxiSystem = new TaxiSystem
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  stage = new PrimaryStage() {
    title = "Taxi System"
    width = boardWidth
    height = boardHeight
    scene = createScene()
  }

  private def createScene():Scene = {
    new Scene {
      fill = WhiteSmoke
      val canvas = new Group

      createStaticMap(canvas)
      createLegend(canvas)

      Thread.sleep(5000)
      val response = taxiSystem.receive

      val responseTaxiState: TaxiSystemState = Await.result(response, Duration(2, TimeUnit.SECONDS))

      val responseTaxis = responseTaxiState.taxis

      //      println(responseTaxis)
      //      val taxis = List(Taxi("Taxi1", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free),
      //        Taxi("Taxi2", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free),
      //        Taxi("Taxi3", Location(Random.nextInt(boardWidth - 50), Random.nextInt(boardHeight - 50)), TaxiState.Free)
      //      )

      val taxiLabels: List[Text] = responseTaxis.map(t => createText(t.id, t.location))

      val taxiCircles: List[Circle] = responseTaxis.map(t => drawTaxi(t))
      taxiCircles.foreach(tc => canvas.getChildren.add(tc))
      taxiLabels.foreach(label => canvas.getChildren.add(label))

      val taxiWithLabel = (taxiLabels, taxiCircles).zipped


      //      var responseTaxi: List[Taxi]

      response.andThen({
        case Success(response) => response
        case Failure(_) => None
      })

      content = canvas

      var lastUpdate = 0L
      val timer: AnimationTimer = AnimationTimer { now => {
        if (now - lastUpdate >= refreshTime) {
          taxiWithLabel.foreach((label, taxi) => canvas.children.removeAll(label, taxi))
          taxiWithLabel.foreach((label, taxi) => {
            taxi.fill = checkState(randomState())
            val newLocation = getRandomLocation
            taxi.relocate(newLocation.x, newLocation.y)
            label.relocate(newLocation.x + padding, newLocation.y + padding)

          })
          taxiWithLabel.foreach((label, taxi) => canvas.children.addAll(label, taxi))
          lastUpdate = now
        }
      }
      }

      timer.start()
    }
  }

  private def createLegend(canvas: Group) = {
    canvas.getChildren.add(createText("free taxi", Location(boardWidth - 200, 0)))
    canvas.getChildren.add(createCircle(boardWidth - 200, 25, freeStateColor))
    canvas.getChildren.add(createText("on the way to customer taxi", Location(boardWidth - 200, 50)))
    canvas.getChildren.add(createCircle(boardWidth - 200, 75, onWayToCustomerStateColor))
    canvas.getChildren.add(createText("occupied taxi", Location(boardWidth - 200, 100)))
    canvas.getChildren.add(createCircle(boardWidth - 200, 125, occupiedStateColor))
  }

  private def createText(name: String, location: Location): Text = {
    val text = new Text(name)
    text.fill = Black
    text.relocate(location.x + padding, location.y + padding)
    text.boundsType = TextBoundsType.Visual
    text
  }

  private def createStaticMap(canvas: Group): Unit = {
    val circles: List[Circle] = cityMap.getCityMapElements.nodes.map(node => createCircle(node.location.x, node.location.y, Blue))
    val paths: List[Path] = cityMap.getCityMapElements.edges.map(edge => generatePath(List(new Point2D(edge.first.location.x, edge.first.location.y),
      new Point2D(edge.second.location.x, edge.second.location.y))))
    circles.foreach(circle => canvas.getChildren.add(circle))
    paths.foreach(path => canvas.getChildren.add(path))
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
  private def randomState(): TaxiState = {
    val number = Random.nextInt(3)
    number match {
      case 0 => TaxiState.Free
      case 1 => TaxiState.Occupied
      case 2 => TaxiState.OnWayToCustomer
    }
  }

  private def getRandomLocation: Location = {
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
  val boardWidth = 800
  val boardHeight = 800
  val refreshTime = 1000000000L
  val padding = 18
  val freeStateColor = Green
  val occupiedStateColor = Red
  val onWayToCustomerStateColor = DarkMagenta
}
