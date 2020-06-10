package com.teamg.taxi.core.actors
import actors.ManagerActor
import akka.actor.{ActorSystem, Props}


class ActorExample {

  val system = ActorSystem("TaxiManagement")
  val manager = system.actorOf(Props(classOf[ManagerActor]), "manager")


  //create 4 Taxis
  createTaxis(4)



  implicit val executor = system.dispatcher

  def createTaxis(number: Int): Unit ={
    for (i <- 1 to number) {
      val taxi = Taxi(i, Location(1.0f, 1.0f))
      // register the new taxi with the management center
      manager ! NewTaxi(taxi)
    }
  }
}



