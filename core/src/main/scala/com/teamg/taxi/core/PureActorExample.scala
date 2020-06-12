package com.teamg.taxi.core

import akka.actor.{ActorSystem, Props}
import com.teamg.taxi.core.actors.ManagerActor.messages.{DispatchOrderM, StartM}
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.ArrivedOrderM
import com.teamg.taxi.core.actors.{ManagerActor, OrderAllocationManagerActor}
import com.teamg.taxi.core.factory.OrderFactory
import com.teamg.taxi.core.model.{CustomerType, OrderType}


class PureActorExample {
  private val system: ActorSystem = ActorSystem("TaxiManagement")

  private val managerActor = system.actorOf(Props(classOf[ManagerActor]), "manager")
  managerActor ! StartM

  managerActor ! DispatchOrderM(OrderFactory.create("A", "C", CustomerType.Normal, OrderType.Normal))
  //  managerActor ! DispatchOrderM(OrderFactory.create("A", "B", ClientType.Normal, OrderType.Normal))
  //  managerActor ! DispatchOrderM(OrderFactory.create("B", "D", ClientType.Normal, OrderType.Normal))


  private val orderAllocationManagerActor = system.actorOf(Props(classOf[OrderAllocationManagerActor]), "orderAllocationManager")

  orderAllocationManagerActor ! ArrivedOrderM(OrderFactory.create("from1", "target1", CustomerType.Normal, OrderType.Normal))
  Thread.sleep(5000)

  println("add order2")
  orderAllocationManagerActor ! ArrivedOrderM(OrderFactory.create("from2", "target2", CustomerType.Normal, OrderType.Normal))


}



