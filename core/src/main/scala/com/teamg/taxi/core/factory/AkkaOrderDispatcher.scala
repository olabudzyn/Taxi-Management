package com.teamg.taxi.core.factory
import akka.actor.ActorRef
import com.teamg.taxi.core.actors.OrderAllocationManagerActor.messages.ArrivedOrderM
import com.teamg.taxi.core.model.Order

import scala.concurrent.{ExecutionContext, Future}

class AkkaOrderDispatcher(orderAllocationManager: ActorRef) extends OrderDispatcher {
  override def dispatch(order: Order)
                       (implicit executionContext: ExecutionContext): Future[Unit] =
    Future(orderAllocationManager ! ArrivedOrderM(order))

}
