package com.teamg.taxi.core.actors


import java.time.{Clock, ZoneId}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{GetUnallocatedOrdersM, SendTaxis}
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartM, StopM, UpdateTaxiLocationsM}
import com.teamg.taxi.core.actors.resource.ResourceActor
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.UpdateLocationM
import com.teamg.taxi.core.actors.systemwatcher.SystemStateWatcher.messages.{TaxiStateM, UnallocatedOrdersM}
import com.teamg.taxi.core.actors.systemwatcher.TaxiSystemStateFetcher
import com.teamg.taxi.core.api
import com.teamg.taxi.core.api.{Location, OrderService, SystemService, TaxiSystemState}
import com.teamg.taxi.core.factory.AkkaOrderDispatcher
import com.teamg.taxi.core.map.MapProvider
import com.teamg.taxi.core.model.{Taxi, TaxiState, TaxiType}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class TaxiSystemActor(taxiIds: List[String])
  extends Actor
    with ActorLogging
    with Timers
    with TaxiSystemStateFetcher {

  implicit val clock: Clock = Clock.system(ZoneId.of("Europe/Warsaw"))
  private val scale = 20
  private lazy val orderAllocationManager = context.actorOf(Props(classOf[OrderAllocationManagerActor], clock))
  private lazy val taxiActors = createTaxiActors(taxiIds)
  private val cityMap = MapProvider.default

  private implicit val system: ActorSystem = ActorSystem("TaxiSystemManagement")
  private implicit val materializer: Materializer = Materializer(context)

  import system.dispatcher

  private val orderService = new OrderService(orderDispatcher = new AkkaOrderDispatcher(orderAllocationManager))
  private val stateService = new SystemService(this)

  private val route = orderService.route ~ stateService.route

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080).andThen {
    case Success(_) => println("Bind success")
    case Failure(_) => println("Bind failure")
  }

  private val taxisStates: mutable.Map[String, api.Taxi] = mutable.Map(taxiActors.map(entry => entry._1 -> defaultTaxiState(entry._1)).toSeq: _*)

  private def defaultTaxiState(id: String): api.Taxi = {
    api.Taxi(id, Location(0.0, 0.0), api.TaxiState.Free)
  }

  def receive: Receive = {
    case StartM =>
      log.debug("StartM")
      orderAllocationManager ! SendTaxis(taxiActors)
      timers.startTimerAtFixedRate("UpdateKey", UpdateTaxiLocationsM, 1.second)

    case UpdateTaxiLocationsM =>
      taxiActors.foreach(entry => entry._2 ! UpdateLocationM(scale))

    case TaxiStateM(id, location, taxiState) =>
      val apiLocation = Location(location.x, location.y)
      val apiTaxiState = taxiState match {
        case _: TaxiState.Free => api.TaxiState.Free
        case _: TaxiState.Occupied => api.TaxiState.Occupied
        case _: TaxiState.OnWayToCustomer => api.TaxiState.OnWayToCustomer
      }
      taxisStates.update(id, api.Taxi(id, apiLocation, apiTaxiState))

    case StopM =>
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done

  }

  private def createTaxiActors(taxiIds: List[String]): Map[String, ActorRef] = {
    taxiIds.map(id =>
      id -> context.actorOf(Props(classOf[ResourceActor], clock, Taxi(id, TaxiType.Car), orderAllocationManager, cityMap.randomNode()))
    ).toMap
  }

  override def getTaxiSystemState(implicit executionContext: ExecutionContext): Future[TaxiSystemState] = {
    implicit val timeout = Timeout(15 seconds)
    ask(orderAllocationManager, GetUnallocatedOrdersM)
      .map(_.asInstanceOf[UnallocatedOrdersM].orders
        .map(order => {
          val location = cityMap.getNode(order.from).map(_.location).get
          api.Order(order.id, Location(location.x, location.y))
        }))
      .map(orders => TaxiSystemState(orders, taxisStates.values.toList))
  }
}

object TaxiSystemActor {

  object messages {

    case object StartM

    case object StopM

    case object UpdateTaxiLocationsM

  }

}
