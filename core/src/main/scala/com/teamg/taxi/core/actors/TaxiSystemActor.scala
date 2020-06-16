package com.teamg.taxi.core.actors


import java.time.{Clock, ZoneId}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.{GetUnallocatedOrdersM, SendTaxis}
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StopM, TaxiStateM, UnallocatedOrdersM}
import com.teamg.taxi.core.actors.resource.ResourceActor
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{GetTaxiStateM, UpdateLocationM}
import com.teamg.taxi.core.api.{OrderService, SystemService, TaxiSystemState}
import com.teamg.taxi.core.factory.{AkkaOrderDispatcher, TaxiSystemStateFetcher}
import com.teamg.taxi.core.map.Location
import com.teamg.taxi.core.model.{Order, Taxi, TaxiState}
import com.teamg.taxi.core.{SimulationConfig, api}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class TaxiSystemActor(config: SimulationConfig)
  extends Actor
    with ActorLogging
    with Timers
    with TaxiSystemStateFetcher {

  implicit val clock: Clock = Clock.system(ZoneId.of("Europe/Warsaw"))
  private lazy val orderAllocationManager = context.actorOf(Props(classOf[OrderAllocationManagerActor], config, clock, system.dispatcher))
  private lazy val taxiActors: Map[String, ActorRef] =
    config.taxis.map(entry => entry._1 -> context.actorOf(Props(classOf[ResourceActor], clock, entry._2, orderAllocationManager, config.cityMap)))

  private implicit val system: ActorSystem = ActorSystem("TaxiSystem")
  private implicit val materializer: Materializer = Materializer(context)

  import system.dispatcher

  private val orderService = new OrderService(orderDispatcher = new AkkaOrderDispatcher(orderAllocationManager))
  private val stateService = new SystemService(this)

  private val route = orderService.route ~ stateService.route

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080).andThen {
    case Success(_) => println("Bind success")
    case Failure(_) => println("Bind failure")
  }

  private val taxisStates: mutable.Map[String, api.Taxi] = mutable.Map(config.taxis.map(entry => entry._1 -> initialTaxiState(entry._2)).toSeq: _*)

  private def initialTaxiState(taxi: Taxi): api.Taxi = {
    api.Taxi(taxi.id, api.Location(taxi.defaultNode.location.x, taxi.defaultNode.location.y), api.TaxiState.Free)
  }

  system.scheduler.scheduleWithFixedDelay(1 second, 3 seconds)(() => taxiActors.foreach(entry => entry._2 ! GetTaxiStateM))
  system.scheduler.scheduleWithFixedDelay(1 second, 3 seconds)(() => taxiActors.foreach(entry => entry._2 ! UpdateLocationM(config.stepSize)))
  system.scheduler.scheduleOnce(1 second)(orderAllocationManager ! SendTaxis(taxiActors))

  def receive: Receive = {
    case TaxiStateM(id, location, taxiState) =>
      val apiLocation = api.Location(location.x, location.y)
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

  override def getTaxiSystemState(implicit executionContext: ExecutionContext): Future[TaxiSystemState] = {
    implicit val timeout = Timeout(15 seconds)
    ask(orderAllocationManager, GetUnallocatedOrdersM)
      .map(_.asInstanceOf[UnallocatedOrdersM].orders
        .map(order => {
          val location = config.cityMap.getNode(order.from).map(_.location).get
          api.Order(order.id, api.Location(location.x, location.y))
        }))
      .map(orders => TaxiSystemState(orders, taxisStates.values.toList))
  }
}

object TaxiSystemActor {

  object messages {

    case class TaxiStateM(id: String, location: Location, taxiState: TaxiState)

    case class UnallocatedOrdersM(orders: List[Order])

    case object StopM

  }
}
