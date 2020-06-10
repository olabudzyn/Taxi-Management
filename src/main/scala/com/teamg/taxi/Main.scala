package com.teamg.taxi

import com.teamg.taxi.actors.ActorExample
import com.teamg.taxi.map.MapExample

object Main extends App {
  val actorExample = new ActorExample()

  new MapExample().run()
}


