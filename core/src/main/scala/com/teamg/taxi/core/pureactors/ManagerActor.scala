package com.teamg.taxi.core.pureactors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}
import com.teamg.taxi.core.map.{Edge, MapProvider}
import com.teamg.taxi.core.model.{Order, Taxi, TaxiType}
import com.teamg.taxi.core.pureactors.ManagerActor.messages.{DispatchOrderM, StartM, UpdateTaxiLocationsM}
import com.teamg.taxi.core.pureactors.ResourceActor.messages.{SetTargetM, UpdateLocationM}
import com.teamg.taxi.core.pureactors.LocationUtils._

import scala.concurrent.duration._

class ManagerActor
  extends Actor
    with ActorLogging
    with Timers {

  private val taxiIds = List("1", "2", "3")
  private var taxiActors = Map.empty[String, ActorRef]
  private val scale = 1
  private val cityMap = MapProvider.default
  private val defaultTaxi = taxiIds.head

  def sendOrderToTaxi(edges: List[Edge[String]], taxi: ActorRef): Unit = {
    taxi ! SetTargetM(edges)
  }

  def receive: Receive = {
    case StartM =>
      log.debug("StartM")
      createTaxiActors()
      timers.startTimerAtFixedRate("AAA", UpdateTaxiLocationsM, 1.second)
    case UpdateTaxiLocationsM =>
      taxiActors.foreach(entry => entry._2 ! UpdateLocationM(scale))
    case DispatchOrderM(order) =>
      for {
        edges <- cityMap.edges(order.from, order.target)
        taxi <- taxiActors.get(defaultTaxi) // TODO chose proper taxi
      } yield sendOrderToTaxi(edges, taxi)
  }

  private def createTaxiActors(): Unit = {
    taxiIds.foreach(id => {
      val child = context.actorOf(Props(classOf[ResourceActor], Taxi(id, TaxiType.Car), randomLocation()))
      taxiActors += (id -> child)
    })
  }

}

object ManagerActor {

  object messages {

    case object StartM

    case object UpdateTaxiLocationsM

    case class DispatchOrderM(order: Order)

  }

}
