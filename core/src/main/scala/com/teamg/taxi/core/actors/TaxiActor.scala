package actors
import akka.actor.{Actor, ActorRef, Props}
import com.teamg.taxi.core.actors.{GetLocation, LocationReport, SetNewLocation, Taxi, TaxiLocationReport}


/* agent representing the driver */
class TaxiActor(private var taxi:Taxi, manager: ActorRef) extends Actor {

  //location actor for this taxi
  private val locationActor = context.actorOf(Props(classOf[LocationActor]), "locationActor")

  override def receive = {
    case LocationReport =>
      println(s"TaxiActor ${taxi.id} receive message to report location")
      locationActor ! GetLocation(taxi)

    case SetNewLocation(newLocation) =>
      taxi = taxi.copy(location = newLocation)
      println(s"TaxiActor ${taxi.id} set new location")
      manager ! TaxiLocationReport(taxi)
  }


}
