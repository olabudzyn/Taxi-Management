package com.teamg.taxi.core.actors

import java.time.{Clock, Instant}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import cats.Eq
import cats.implicits._
import com.teamg.taxi.core.SimulationConfig
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.UnallocatedOrdersM
import com.teamg.taxi.core.actors.order.SchedulingHelper._
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, NewOrderRequestM}
import com.teamg.taxi.core.model.TaxiType.Van
import com.teamg.taxi.core.model._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class OrderAllocationManagerActor(config: SimulationConfig,
                                  clock: Clock,
                                  executionContext: ExecutionContext) extends Actor with ActorLogging{
  private val actorSystem = ActorSystem("OrderAllocationManager")

  private var taxiActors: Map[String, ActorRef] = Map.empty
  private var taxiStates: Map[String, TaxiPureState] = Map.empty

  private val orders: mutable.Map[String, Order] = mutable.Map.empty
  private val ordersmap: mutable.Map[String, OrderRequest] = mutable.Map(Seq.empty[(String, OrderRequest)]: _*)

  implicit val ec = executionContext

  override def receive: Receive = {
    case ArrivedOrderM(order: Order) =>
      orders(order.id) = order
      val timeToDecision = getTimeToDecision(order)
      ordersmap(order.id) = OrderRequest(timeToDecision.toEpochMilli, order)
      log.info(s"Received order with id: [${order.id}]")

    case SendTaxis(taxis) =>
      taxiActors = taxis
      taxiStates = createInitialTaxiStates(taxiActors)

    case response: TaxiUpdateResponse =>
      response match {
        case TaxiUpdateResponse.TaxiFinishedOrderM(taxi, timestamp) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Free(timestamp))
        case TaxiUpdateResponse.TaxiPickUpCustomerM(taxi, id) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Occupied)
          ordersmap.remove(id)
          orders.remove(id)
      }

    case response: TaxiOrderResponse =>
      response match {
        case TaxiOrderResponse.TaxiOnWayToCustomerM(taxi) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.OnWayToCustomer)
        case TaxiOrderResponse.TaxiPickUpCustomerM(taxi, id) =>
          taxiStates = taxiStates.updated(taxi.id, TaxiPureState.Occupied)
          ordersmap.remove(id)
        case TaxiOrderResponse.TaxiAlreadyOccupiedM(taxi) =>
        case TaxiOrderResponse.TaxiOnWayToAnotherClient(taxi) =>
      }

    case GetUnallocatedOrdersM =>
      sender ! UnallocatedOrdersM(ordersmap.values.map(_.order).toList)

  }

  actorSystem.scheduler.scheduleAtFixedRate(10 seconds, 100 seconds)(() => scheduleTaxis())

  private def scheduleTaxis(): Future[List[Unit]] = {
    val orderBidsFuture = ordersmap
      .map(entry => entry._1 -> entry._2.order)
      .map(entry => getBids(entry._2).map(bids => OrderBids(entry._2, bids)))

    Future.sequence(orderBidsFuture)
      .map(_.toList)
      .map(list => assignToTaxis(list))
      .map { taxiOrders =>
        taxiOrders.map(orderTaxi => sendOrderToTaxi(orderTaxi._2, orderTaxi._1)).toList
      }
  }

  private def getBids(order: Order): Future[Map[String, Double]] = {
    for {
      filtered <- getPossibleTaxisByType(order.taxiType)
      bids <- getBidsFromActors(order, filtered.keys.toList)
    } yield bids
  }

  private def getPossibleTaxisByType(taxiType: TaxiType): Future[Map[String, Taxi]] = {
    Future.successful {
      taxiType match {
        case TaxiType.Van => config.taxis.filter(_._2.taxiType === Van)
        case TaxiType.Car => config.taxis
      }
    }
  }

  private def getBidsFromActors(order: Order, ids: List[String]): Future[Map[String, Double]] = {
    implicit val timeout = Timeout(10 seconds)

    val listOfFutures = ids.map { id =>
      val actor = taxiActors(id)
      ask(actor, CalculateCostM(order))
        .map(_.asInstanceOf[TaxiCostResponse].cost)
        .map(cost => id -> cost)
    }

    val futureMap = Future.sequence(listOfFutures.map(_.filter(_._2.isDefined)))
      .map(_.toMap.collect { case (key, Some(value)) => (key, value) })

    futureMap
  }

  private def sendOrderToTaxi(order: Order, taxi: String): Unit = {
    log.info(s"Order(${order.id}) from:[${order.from}] target:[${order.target}] sending to taxi: $taxi")
    taxiActors(taxi) ! NewOrderRequestM(order)
  }

  private def createInitialTaxiStates(taxiActors: Map[String, ActorRef]): Map[String, TaxiPureState] = {
    val timestamp = clock.instant()
    taxiActors.map(p =>
      p._1 -> TaxiPureState.Free(timestamp)
    )
  }

  implicit val eqFoo: Eq[TaxiPureState] = Eq.fromUniversalEquals
  implicit val eqTaxiType: Eq[TaxiType] = Eq.fromUniversalEquals
}


object OrderAllocationManagerActor {

  object messages {

    sealed trait TaxiOrderResponse

    object TaxiOrderResponse {

      case class TaxiAlreadyOccupiedM(taxi: Taxi) extends TaxiOrderResponse

      case class TaxiPickUpCustomerM(taxi: Taxi, id: String) extends TaxiOrderResponse

      case class TaxiOnWayToCustomerM(taxi: Taxi) extends TaxiOrderResponse

      case class TaxiOnWayToAnotherClient(taxi: Taxi) extends TaxiOrderResponse

    }

    sealed trait TaxiUpdateResponse

    object TaxiUpdateResponse {

      case class TaxiFinishedOrderM(taxi: Taxi, orderCompletionTimestamp: Instant) extends TaxiUpdateResponse

      case class TaxiPickUpCustomerM(taxi: Taxi, id: String) extends TaxiUpdateResponse

    }

    case object GetUnallocatedOrdersM

    case object CreateOrderActorsM

    case class ArrivedOrderM(order: Order)

    case class DispatchOrderToTaxiM(order: Order)

    case class SendTaxis(taxiActors: Map[String, ActorRef])

    case class TaxiCostResponse(taxi: Taxi, cost: Option[Double])

  }

}
