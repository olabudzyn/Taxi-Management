package com.teamg.taxi.core

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.{ActorSystem, Props}
import cats.implicits._
import com.teamg.taxi.core.actors.TaxiSystemActor
import com.teamg.taxi.core.actors.TaxiSystemActor.messages.{StartM, StopM}
import com.teamg.taxi.core.factory.OrderSender
import com.teamg.taxi.core.service.OrderService.OrderRequest


class PureActorExample {

  private implicit val system: ActorSystem = ActorSystem("TaxiManagement")
  implicit val executionContext = system.dispatcher

  val taxiLabels = List("Taxi1", "Taxi2", "Taxi3", "Taxi4", "Taxi5", "Taxi6", "Taxi7", "Taxi8", "Taxi9", "Taxi10")
  private val taxiSystemActor = system.actorOf(Props(classOf[TaxiSystemActor], taxiLabels), "manager")
  taxiSystemActor ! StartM

  val orderSender = new OrderSender("http://localhost:8080/order")

  val orderRequests: List[OrderRequest] =
    OrderRequest("A", "C", "normal", "normal") ::
      OrderRequest("A", "D", "vip", "predefined", Some(Instant.now().plus(30, ChronoUnit.SECONDS))) :: Nil

  orderRequests.traverse(request => orderSender.send(request))

  Thread.sleep(100000)

  taxiSystemActor ! StopM

}



