package com.teamg.taxi.actors
import akka.actor.{Actor, ActorRef}


/*agent representing course order */
class OrderActor(manager: ActorRef) extends Actor {
  var countDown = 100

      def receive = {
        case NewOrder =>
          println(s"${self.path} zamowienie przyjete")
          manager ! Order
      }


}
