package com.teamg.taxi.core.actors.systemwatcher

import com.teamg.taxi.core.map.Location
import com.teamg.taxi.core.model.{Order, TaxiState}

object SystemStateWatcher {

  object messages {

    case class TaxiStateM(id: String, location: Location, taxiState: TaxiState)

    case class UnallocatedOrdersM(orders: List[Order])

  }

}

