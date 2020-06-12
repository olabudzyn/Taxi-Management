package com.teamg.taxi.core.actors

import java.time.ZoneId

import akka.actor.Actor
import com.teamg.taxi.core.model.{Order, OrderState}
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM, TaxiPickedUpM}
import java.text.SimpleDateFormat
import java.util.TimeZone

class OrderActor(order: Order) extends Actor {

  var formatter = new SimpleDateFormat("HH:mm:ss")
  formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Warsaw")))
  var date = formatter.format(order.timeStamp.toEpochMilli)
  var orderState: OrderState = OrderState.Pending(order, order.timeStamp)

  override def receive: Receive = {

    case PrintOrderActorIdM =>
      println(s"orderId: ${order.id}, createTime: ${date}")

    case StopOrderActorM =>
      context.stop(self)

    case TaxiPickedUpM(taxiId: String) =>
      orderState = OrderState.TransportedCustomer(order, taxiId)

  }
}

object OrderActor {

  object messages {

    case object PrintOrderActorIdM

    case object StopOrderActorM

    case class TaxiPickedUpM(taxiId: String)

  }

}
