package actors

import akka.actor.{Actor, ActorLogging, Props}
import com.teamg.taxi.core.actors.{TaxiLocationReport, NewTaxi, LocationReport}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent._
import ExecutionContext.Implicits.global


class ManagerActor extends Actor with ActorLogging {

  def receive = {
    case NewTaxi(taxi) =>
      val taxiActor = context.actorOf(Props(classOf[TaxiActor], taxi, context.self))
      println(s"Create TaxiActor for taxi ${taxi.id}")

      // run scheduler to call taxiActor to report its location every x millisecond
      context.system.scheduler.scheduleAtFixedRate(3000 millisecond, 6000 millisecond, taxiActor, LocationReport)

      
    case TaxiLocationReport(taxi) =>
      println(s"LOCATION taxi ${taxi.id} : ${taxi.location} ")

  }




}
