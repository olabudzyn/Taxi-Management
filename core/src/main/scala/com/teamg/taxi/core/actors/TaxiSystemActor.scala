package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartDispatchingM, StartM, UpdateTaxiLocationsM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{DispatchOrderToTaxiM, SendTaxis}
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.UpdateLocationM
import com.teamg.taxi.core.actors.resource.ResourceActor
import com.teamg.taxi.core.factory.OrderFactory
import com.teamg.taxi.core.map.MapProvider
import com.teamg.taxi.core.model.{CustomerType, OrderType, Taxi, TaxiType}

import scala.concurrent.duration._

class TaxiSystemActor(taxiIds: List[String]) extends Actor with ActorLogging with Timers {

  private val scale = 0.2
  private val orderAllocationManager = context.actorOf(Props(classOf[OrderAllocationManagerActor]))
  private val taxiActors = createTaxiActors(taxiIds)
  private val cityMap = MapProvider.default


  def receive: Receive = {
    case StartM =>
      log.debug("StartM")
      orderAllocationManager ! SendTaxis(taxiActors)
      orderAllocationManager ! DispatchOrderToTaxiM(OrderFactory.create("A", "C", CustomerType.Normal, OrderType.Normal))
      timers.startTimerAtFixedRate("AAA", UpdateTaxiLocationsM, 1.second)

    case UpdateTaxiLocationsM =>
      taxiActors.foreach(entry => entry._2 ! UpdateLocationM(scale))

    case StartDispatchingM =>
  }


  private def createTaxiActors(taxiIds: List[String]): Map[String, ActorRef] = {
    taxiIds.map(id =>
      id -> context.actorOf(Props(classOf[ResourceActor], Taxi(id, TaxiType.Car),
        orderAllocationManager, cityMap.randomNode()))
    ).toMap

  }

}

object TaxiSystemActor {

  object messages {

    case object StartM

    case object UpdateTaxiLocationsM

    case object StartDispatchingM

  }

}
