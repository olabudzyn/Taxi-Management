package actors
import akka.actor.Actor
import com.teamg.taxi.core.actors.{GetLocation, SetNewLocation, Taxi, Location}

import scala.util.Random

class LocationActor extends Actor{

  override def receive = {
    case GetLocation(taxi) =>
      val location = getRandomLocation(taxi)
      println(s"LocationActor ${taxi.id} give new location")
      sender ! SetNewLocation(location)
  }


  // function to change taxi's location
  def getRandomLocation(taxi: Taxi) = Location(
    taxi.location.lat + Random.nextFloat - 0.5f,
    taxi.location.long + Random.nextFloat - 0.5f)

}

