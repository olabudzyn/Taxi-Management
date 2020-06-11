package com.teamg.taxi.core.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.teamg.taxi.core.pureactors.TaxiActorExample


class ManagerActor extends Actor with ActorLogging {

  var actorsIdList: List[String] = List("1", "2", "3")
  private var TaxiActorExamplesMap = Map.empty[String, ActorRef]


  def receive = {
    case Test =>
      createTaxiActors()
      TaxiActorExamplesMap.foreach(p => p._2 ! Test)

    case TaxiLocationReport(taxi) =>
      println(s"LOCATION taxi ${taxi.id} : ${taxi.location} ")

  }


  def createTaxiActors() = {
    actorsIdList.foreach(p => {
      val child = context.actorOf(Props(classOf[TaxiActorExample], p))
      TaxiActorExamplesMap += (p -> child)
    })
  }

}
