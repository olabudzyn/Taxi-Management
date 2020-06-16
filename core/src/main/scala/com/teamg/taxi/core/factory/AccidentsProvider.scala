package com.teamg.taxi.core.factory

import com.teamg.taxi.core.map.Accident

trait AccidentsProvider {
  def getAccidents: List[Accident[String]]
}
