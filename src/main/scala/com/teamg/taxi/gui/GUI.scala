package com.teamg.taxi.gui

import scalafx.animation.{FillTransition, PathTransition}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.{Group, Scene}
import scalafx.scene.shape.{Circle, LineTo, MoveTo, Path, PathElement, Shape}
import scalafx.util.Duration
import scalafx.scene.paint.Color._

import scala.util.Random

object GUI extends JFXApp {

  val duration = 8000
  val pointSize = 10
  val boardWidth = 800
  val boardHeight = 800

  stage = new PrimaryStage {
    title = "Taxi System"
    width = boardWidth
    height = boardHeight
    scene = new Scene {
      fill = WhiteSmoke
      val canvas = new Group()

      val pointFirst: Point2D = generatePoint()
      val point1: Point2D = generatePoint()
      val point2: Point2D = generatePoint()
      val point3: Point2D = generatePoint()
      val pointLast: Point2D = generatePoint()
      val list = List(pointFirst, point1, point2, point3, pointLast)

      val pointFirst2: Point2D = generatePoint()
      val pointLast2: Point2D = generatePoint()
      val list2 = List(pointFirst2, pointLast2)

      val circle1 = new Circle()
      circle1.fill = Green
      circle1.radius = pointSize
      circle1.relocate(pointFirst.x - pointSize, pointFirst.y - pointSize)

      val circle2 = new Circle()
      circle2.fill = Green
      circle2.radius = pointSize
      circle2.relocate(pointLast.x - pointSize, pointLast.y - pointSize)

      val circle3 = new Circle()
      circle3.fill = Green
      circle3.radius = pointSize
      circle3.relocate(pointFirst2.x - pointSize, pointFirst2.y - pointSize)

      val circle4 = new Circle()
      circle4.fill = Green
      circle4.radius = pointSize
      circle4.relocate(pointLast2.x - pointSize, pointLast2.y - pointSize)

      applyAnimation(canvas, circle1, circle2, list)
      applyAnimation(canvas, circle3, circle4, list2)
      content = canvas

      def generatePoint(): Point2D = {
        val r = Random
        val x = 50 + r.nextInt(boardWidth - 100)
        val y = 50 + r.nextInt(boardHeight - 100)
        return new Point2D(x, y)
      }

      def generatePath(points: List[Point2D]): Path = {
        val path = new Path
        val first = points.head
        val listToElement: List[PathElement] = points.map(e => LineTo(e.x, e.y))
        path.elements.add(MoveTo(first.x, first.y))
        listToElement.foreach(e => path.elements.add(e))
        path.setOpacity(1)
        return path
      }

      def generatePathTransition(shape: Shape, path: Path): PathTransition = {
        val pathTransition = new PathTransition();
        pathTransition.setDuration(new Duration(duration));
        pathTransition.setPath(path);
        pathTransition.setNode(shape);
        pathTransition.setOrientation(PathTransition.OrientationType.OrthogonalToTangent);
        pathTransition.setCycleCount(1);
        return pathTransition;
      }

      def generateFillTransition(circle: Circle): FillTransition = {
        val fillTransition = new FillTransition();
        fillTransition.duration = new Duration(500)
        fillTransition.delay = new Duration(duration)
        fillTransition.fromValue = Green
        fillTransition.toValue = Yellow
        fillTransition.shape = circle
        return fillTransition;
      }

      def applyAnimation(group: Group, circle1: Circle, circle2: Circle, points: List[Point2D]): Unit = {
        val circle = new Circle()
        circle.fill = Red
        circle.radius = pointSize
        val path = generatePath(points);
        group.getChildren.add(path)
        group.getChildren.add(circle)
        group.getChildren.add(circle1)
        group.getChildren.add(circle2)
        val transition = generatePathTransition(circle, path);
        transition.play()
        val colourTrans = generateFillTransition(circle2)
        colourTrans.play()
        val colourTrans2 = generateFillTransition(circle1)
        colourTrans2.play()
      }
    }
  }
}
