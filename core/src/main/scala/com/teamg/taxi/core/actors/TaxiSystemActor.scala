package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.SendTaxis
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartDispatchingM, StartM, StopM, UpdateTaxiLocationsM}
import com.teamg.taxi.core.actors.resource.ResourceActor
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.UpdateLocationM
import com.teamg.taxi.core.factory.AkkaOrderDispatcher
import com.teamg.taxi.core.map.MapProvider
import com.teamg.taxi.core.model.{Taxi, TaxiType}
import com.teamg.taxi.core.service.OrderService

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TaxiSystemActor(taxiIds: List[String]) extends Actor with ActorLogging with Timers {

  private val scale = 0.2
  private val orderAllocationManager = context.actorOf(Props(classOf[OrderAllocationManagerActor]))
  private lazy val taxiActors = createTaxiActors(taxiIds)
  private val cityMap = MapProvider.default

  private implicit val system: ActorSystem = ActorSystem("TaxiSystemManagement")
  private implicit val materializer: Materializer = Materializer(context)
  import system.dispatcher

  private val orderService = new OrderService(orderDispatcher = new AkkaOrderDispatcher(orderAllocationManager))
  private val bindingFuture = Http().bindAndHandle(orderService.route, "localhost", 8080).andThen  {
    case Success(_) => println("Bind success")
    case Failure(_) => println("Bind failure")
  }

  def receive: Receive = {
    case StartM =>
      log.debug("StartM")
      orderAllocationManager ! SendTaxis(taxiActors)
      timers.startTimerAtFixedRate("UpdateKey", UpdateTaxiLocationsM, 1.second)


    case UpdateTaxiLocationsM =>
      taxiActors.foreach(entry => entry._2 ! UpdateLocationM(scale))

    case StartDispatchingM =>


    case StopM =>
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done

  }


  private def createTaxiActors(taxiIds: List[String]): Map[String, ActorRef] = {
    taxiIds.map(id =>
      id -> context.actorOf(Props(classOf[ResourceActor], Taxi(id, TaxiType.Car), orderAllocationManager, cityMap.randomNode()))
    ).toMap

  }

}

object TaxiSystemActor {

  object messages {

    case object StartM

    case object StopM

    case object UpdateTaxiLocationsM

    case object StartDispatchingM

  }

}