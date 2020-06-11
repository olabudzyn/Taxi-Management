package com.teamg.taxi.core

import akka.actor.{ActorSystem, Props}
import com.teamg.taxi.core.factory.OrderFactory
import com.teamg.taxi.core.model.{ClientType, OrderType}
import com.teamg.taxi.core.pureactors.ManagerActor
import com.teamg.taxi.core.pureactors.ManagerActor.messages.{DispatchOrderM, StartM}


class PureActorExample {
  private val system: ActorSystem = ActorSystem("TaxiManagement")

  private val managerActor = system.actorOf(Props(classOf[ManagerActor]), "manager")
  managerActor ! StartM

  managerActor ! DispatchOrderM(OrderFactory.create("A", "C", ClientType.Normal, OrderType.Normal))
//  managerActor ! DispatchOrderM(OrderFactory.create("A", "B", ClientType.Normal, OrderType.Normal))
//  managerActor ! DispatchOrderM(OrderFactory.create("B", "D", ClientType.Normal, OrderType.Normal))

}



