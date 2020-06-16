package com.teamg.taxi.core.map

import scala.math._
import scala.util.Random

object LocationUtils {
  def distance(l1: Location, l2: Location): Double = {
    sqrt(pow(l1.x - l2.x, 2) + pow(l1.y - l2.y, 2))
  }

  def center(l1: Location, l2: Location): Location = {
    Location( (l1.x + l2.x) / 2.0, (l1.y + l2.y) / 2.0)
  }

  def updateLocation(source: Location, target: Location, dist: Double): Location = {
    val alpha = atan2(target.y - source.y, target.x - source.x)
    val nx = dist * cos(alpha) + source.x
    val ny = dist * sin(alpha) + source.y
    Location(nx, ny)
  }

  def randomLocation(): Location = {
    Location(doubleInRange(startX, endX), doubleInRange(startY, endY))
  }

  private def doubleInRange(start: Double, end: Double) = {
    start + (end - start) * Random.nextDouble()
  }

  private val mapSize = 10
  private val startX = 0.0
  private val startY = 0.0
  private val endX = mapSize
  private val endY = mapSize


}
