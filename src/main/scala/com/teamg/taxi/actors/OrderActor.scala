package actors
import akka.actor.{Actor, ActorRef}
import com.teamg.taxi.actors.Order
import com.teamg.taxi.actors.NewOrder


/*agent representing course order */
class OrderActor(manager: ActorRef) extends Actor {
  var countDown = 100

  def receive = {
    case NewOrder =>
      println(s"New order arrived")
      manager ! Order
  }


}
