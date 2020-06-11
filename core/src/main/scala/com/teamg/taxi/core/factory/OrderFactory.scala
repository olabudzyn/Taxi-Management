package com.teamg.taxi.core.factory

import java.util.UUID

import com.teamg.taxi.core.model.{ClientType, Order, OrderType}

object OrderFactory {

  private val idProvider: IdProvider[String] = () => UUID.randomUUID().toString

  def create(from: String,
             target: String,
             clientType: ClientType,
             orderType: OrderType): Order = {
    Order(idProvider.next(), from, target, clientType, orderType)
  }
}
