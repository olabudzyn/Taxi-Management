package com.teamg.taxi.core

import com.teamg.taxi.core.actors.ActorExample
import com.teamg.taxi.core.map.MapExample

object Main extends App {
  val actorExample = new ActorExample()

  new MapExample().run()
}
