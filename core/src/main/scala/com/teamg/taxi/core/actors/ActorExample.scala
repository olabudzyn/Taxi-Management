package com.teamg.taxi.core.actors

import akka.actor.{ActorSystem, Props}


class ActorExample {

  val system = ActorSystem("TaxiManagement")
  val manager = system.actorOf(Props(classOf[ManagerActor]), "manager")

  manager ! Test

  implicit val executor = system.dispatcher


}



