package com.teamg.taxi.core.actors

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import cats.Eq
import cats.implicits._
import com.teamg.taxi.core.SimulationConfig
import com.teamg.taxi.core.actors.OrderActor.messages.{PrintOrderActorIdM, StopOrderActorM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages._
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.UnallocatedOrdersM
import com.teamg.taxi.core.actors.resource.ResourceActor.messages.{CalculateCostM, NewOrderRequestM}
import com.teamg.taxi.core.model.TaxiType.Van
import com.teamg.taxi.core.model.{CustomerType, Order, OrderType, Taxi, TaxiPureState, TaxiType}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class OrderAllocationManagerActor(config: SimulationConfig,
                                  clock: Clock,
                                  executionContext: ExecutionContext) extends Actor {
  implicit val eqTaxiType: Eq[TaxiType] = Eq.fromUniversalEquals
  private var taxiActors: Map[String, ActorRef] = Map.empty
  private var taxiStates: Map[String, TaxiPureState] = Map.empty

  private var orderActors: Map[String, ActorRef] = Map.empty

  private val orders: mutable.Map[String, Order] = mutable.Map.empty

  private val ordersmap: mutable.Map[String, OrderRequest] = mutable.Map(Seq.empty[(String, OrderRequest)]: _*)

  private val actorSystem = ActorSystem("OrderAllocationManager")

  case class OrderRequest(time: Long, order: Order)

  implicit val ec = executionContext

  def getPossibleTaxisByType(taxiType: TaxiType): Future[Map[String, Taxi]] = {
    Future.successful {
      taxiType match {
        case TaxiType.Van => config.taxis.filter(_._2.taxiType === Van)
        case TaxiType.Car => config.taxis
      }
    }
  }

  def getBidsFromActors(order: Order, ids: List[String]): Future[Map[String, Double]] = {
    implicit val timeout = Timeout(10 seconds)

    val listOfFutures = ids.map { id =>
      val actor = taxiActors(id)
      ask(actor, CalculateCostM(order))
        .map(_.asInstanceOf[TaxiCostResponse].cost)
        .map(cost => id -> cost)
    }

    val futureMap = Future.sequence(listOfFutures.map(_.filter(_._2.isDefined)))
      .map(_.toMap.collect { case (key, Some(value)) => (key, value) })
        .andThen {
          case Success(a) => println(s"BIDS SIZE: ${a.size}")
          case Failure(exception) => exception.printStackTrace()
        }

    futureMap
  }

  def choseActor(bids: Map[String, Double]) = {
    Future(taxiActors(bids.minBy(_._2)._1))
  }

  object timesToDecision {
    val normal = 1000
    val vip = 500
    val supervip = 100
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

  case class OrderBids(order: Order, bids: Map[String, Double])

  def scheduleTaxis() = {
    val orderBidsFuture = ordersmap
      .map(entry => entry._1 -> entry._2.order)
      .map(entry => getBids(entry._2).map(bids => OrderBids(entry._2, bids)))

    Future.sequence(orderBidsFuture)
      .map(_.toList)
      .map(list => assignToTaxis(list))
      .map { taxiOrders =>
        taxiOrders.map(orderTaxi => sendOrderToTaxi(orderTaxi._2, taxiActors(orderTaxi._1)))
      }
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

  def getBids(order: Order): Future[Map[String, Double]] = {
    for {
      filtered <- getPossibleTaxisByType(order.taxiType)
      bids <- getBidsFromActors(order, filtered.keys.toList)
    } yield bids
  }


  actorSystem.scheduler.scheduleAtFixedRate(10 seconds, 100 seconds)(() => scheduleTaxis())

  override def receive: Receive = {
    case ArrivedOrderM(order: Order) =>
      orders(order.id) = order
      val timeToDecision = getTimeToDecision(order)
      ordersmap(order.id) = OrderRequest(timeToDecision.toEpochMilli, order)
      println("ORDERS map: " + ordersmap)
//      for {
//        filtered <- getPossibleTaxisByType(order.taxiType)
//        bids <- getBidsFromActors(order, filtered.keys.toList)
//        chosenActor <- choseActor(bids)
//      } yield sendOrderToTaxi(order, chosenActor)

//      getCostFromTaxis(order, taxiActors, taxiStates)
//      taxiStates = createInitialTaxiStates(taxiActors)
//      taxiCost = createInitialTaxiCost(taxiActors)
//      calculateCostFunction(order, taxiCost, taxiStates)
//      addOrderActor(order)
//
//      for {
//        taxi <- taxiActors.get(Utils.getRandomElement(taxiActors.keys.toSeq, new Random())) // TODO chose proper taxi
//      }

    case SendTaxis(taxis) =>
      taxiActors = taxis
      taxiStates = createInitialTaxiStates(taxiActors)

//    case TaxiCostResponse(taxi, cost) =>
//      println(s"${taxi.id} cost: ${cost}")

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

  def addOrderActor(order: Order): Unit = {
    val child = context.actorOf(Props(classOf[OrderActor], order))
    orderActors += (order.id -> child)
  }

  def deleteOrderActor(orderId: String) = {
    orderActors(orderId) ! StopOrderActorM
    orderActors -= orderId
  }

  def printOrderActors() = {
    orderActors.foreach(p => p._2 ! PrintOrderActorIdM)
  }

  def sendOrderToTaxi(order: Order, taxi: ActorRef): Unit = {
    println("send order to taxi")
    taxi ! NewOrderRequestM(order)
  }


  private def createInitialTaxiStates(taxiActors: Map[String, ActorRef]): Map[String, TaxiPureState] = {
    val timestamp = clock.instant()
    taxiActors.map(p =>
      p._1 -> TaxiPureState.Free(timestamp)
    )
  }

  def createInitialTaxiCost(taxiActors: Map[String, ActorRef]): Map[String, Option[Double]] = {
    taxiActors.map(p =>
      p._1 -> None
    ).toMap
  }

  def getCostFromTaxis(order: Order, taxiActors: Map[String, ActorRef], taxiStates: Map[String, TaxiPureState]) = {
    taxiStates
      .filter(p => p._2 match {
        case _: TaxiPureState.Free => true
        case _ => false
      })
      .map(p => taxiActors(p._1) ! CalculateCostM(order))
  }

  def calculateCostFunction(order: Order, taxiCost: Map[String, Option[Double]], taxiStates: Map[String, TaxiPureState]) = {
    println("Calculating cost function")
  }

  implicit val eqFoo: Eq[TaxiPureState] = Eq.fromUniversalEquals

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
