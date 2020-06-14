package com.teamg.taxi.core.actors

import java.time.{Instant}

import cats.implicits._
import com.teamg.taxi.core.model.{Order}

class CostFunction(order: Order, taxiCost: Map[String, Option[Double]], taxiLastOrder: Map[String, Option[Instant]]) {

  var taxiCostDefined: Map[String, Double] = getTaxiCostDefined(taxiCost)
  var taxiLastOrderDefinedMilli: Map[String, Long] = getTaxiLastOrderDefinedMilli(taxiLastOrder)

  def getTaxiCostDefined(taxiCost: Map[String, Option[Double]]): Map[String, Double] = {
    taxiCost.filter(entry => entry._2.isDefined).map(p =>
      p._1 -> p._2.getOrElse(0.0)).toMap
  }

  def getTaxiLastOrderDefinedMilli(taxiLastOrder: Map[String, Option[Instant]]): Map[String, Long] = {
    taxiLastOrder.filter(entry => entry._2.isDefined).map(p =>
      p._1 -> p._2.get.toEpochMilli).toMap
  }

  def getCostAndLastTime(taxiCostDefined: Map[String, Double], taxiLastOrder: Map[String, Option[Instant]]): Map[Double, Long] = {
    taxiCost.filter(entry => entry._2.isDefined).map(p =>
      p._2.getOrElse(0.0) -> (taxiLastOrder(p._1).get.toEpochMilli)).toMap
  }

  /*
    def getTaxiOfMinCost(taxiCostForOrder: Map[String, Double], taxiLastOrderDefinedMilli: Map[String, Long]): Map[String, Double] = {

      val minValues = taxiCostForOrder.minBy(item => item._2)
      val taxiOfMinCost = taxiCostForOrder.filter(item => item._2 === minValues._2)
      val taxiLastOrderOfMinCost = taxiCostForOrder.filter(item => item._1 === minValues._1)


      if (taxiOfMinCost.size === 1) {
        taxiOfMinCost.map(p => p._1 -> p._2).toMap
      } else {
        val taxiId = taxiLastOrderOfMinCost.minBy(_._2)._1
        Map(taxiId -> taxiOfMinCost(taxiId))
      }
    }
  */

  def getTaxiOfMinCost(taxiCostForOrder: Map[String, Double], taxiLastOrderDefinedMilli: Map[String, Long], taxiCostAndLastOrder: Map[Double, Long]): Map[String, Double] = {
    val taxisOfMinCost = taxiCostForOrder.minBy(item => item._2)
    val minByCost = taxiCostAndLastOrder.filter(item => item._1 === taxisOfMinCost._2)
    val minByLastOrder = minByCost.min
    val taxiId = taxiLastOrderDefinedMilli.filter(p => p._2 === minByLastOrder._2).head
    taxiCostForOrder.filter(p => p._1 === taxiId._1)
  }

}
