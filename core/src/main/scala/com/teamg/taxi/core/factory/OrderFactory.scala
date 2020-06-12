package com.teamg.taxi.core.factory

import java.util.UUID

import com.teamg.taxi.core.model.{CustomerType, Order, OrderType}
import java.time.{Clock, ZoneId}

object OrderFactory {

  var clock: Clock = Clock.system(ZoneId.of("Europe/Warsaw"))

  private val idProvider: IdProvider[String] = () => UUID.randomUUID().toString

  def create(from: String,
             target: String,
             customerType: CustomerType,
             orderType: OrderType): Order = {
    Order(idProvider.next(), from, target, customerType, orderType, clock.instant())
  }
}
