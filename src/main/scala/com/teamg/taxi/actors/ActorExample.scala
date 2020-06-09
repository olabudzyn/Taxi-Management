package com.teamg.taxi.actors
import akka.actor.{ActorSystem, Props}
import language.postfixOps
import scala.concurrent.duration._

case class Location(lat: Float, long: Float)
case class Taxi(name: String, location: Location)
case object NewOrder
case object Order
case object Disposition
case object ReportLocation

class ActorExample {
  val system2 = ActorSystem("pingpong")
  val manager = system2.actorOf(Props[OrderAllocationManagerActor], "manager")
  val order = system2.actorOf(Props(classOf[OrderActor], manager), "order")
  //  val resource = system2.actorOf(Props(classOf[ResourceActor], manager), "order")

  import system2.dispatcher
  system2.scheduler.scheduleOnce(500 millis) {
    order ! NewOrder
  }

}
