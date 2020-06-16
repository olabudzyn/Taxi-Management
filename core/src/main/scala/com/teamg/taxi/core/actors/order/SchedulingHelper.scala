package com.teamg.taxi.core.actors.order

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.teamg.taxi.core.model.{CustomerType, Order, OrderType}

object SchedulingHelper {
  case class OrderRequest(time: Long, order: Order)

  case class OrderBids(order: Order, bids: Map[String, Double])

  object timesToDecision {
    val normal = 1000
    val vip = 500
    val supervip = 100
  }

  def assignToTaxis(list: List[OrderBids]): Map[String, Order] = { // taxi -> Order
    val dummy: List[(Order, String)] = list
      .map(orderBids => orderBids.order -> orderBids.bids.minBy(_._2))
      .map(a => (a._1, a._2._1))

    val dummyAssign = dummy
      .groupBy(_._2)
      .filter(entry => entry._2.size == 1)
      .flatMap(a => a._2)
      .map(_.swap)

    dummyAssign
    //    val taxiAndPossibleOffers =
    //      list
    //        .flatMap(orderBid => orderBid.bids.map(bid => bid._1 -> (orderBid.order, bid._2)))
    //        .groupBy(_._1)
    //        .mapValues(_.map(_._2))

    //    val abc: List[(Order, String, Double)] =
    //      list
    //        .flatMap(orderBid => orderBid.bids.map(bid => (orderBid.order, bid._1, bid._2)))
    //
    //    val taxiOrdersPairs =
    //      taxiAndPossibleOffers.flatMap(value => value._2.map(orderBid => (value._1, orderBid._1, orderBid._2))).toList
    //
    //    // teraz potrzeba przefiltrowac
    //
    //    taxiOrdersPairs.filter()
    //  }
  }

  def getTimeToDecision(order: Order): Instant = {
    order.orderType match {
      case OrderType.Normal =>
        order.customerType match {
          case CustomerType.Normal => order.timeStamp.plus(timesToDecision.normal, ChronoUnit.SECONDS)
          case CustomerType.Vip => order.timeStamp.plus(timesToDecision.vip, ChronoUnit.SECONDS)
          case CustomerType.SuperVip => order.timeStamp.plus(timesToDecision.supervip, ChronoUnit.SECONDS)
        }
      case OrderType.Predefined(time) =>
        order.customerType match {
          case CustomerType.Normal => time.minus(timesToDecision.normal, ChronoUnit.SECONDS)
          case CustomerType.Vip => time.minus(timesToDecision.vip, ChronoUnit.SECONDS)
          case CustomerType.SuperVip => time.minus(timesToDecision.supervip, ChronoUnit.SECONDS)
        }
    }
  }
}
