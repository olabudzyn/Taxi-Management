package com.teamg.taxi

import com.teamg.taxi.actors.ActorExample
import com.teamg.taxi.map.MapExample

object Main extends App {
  val model = new ActorExample()

  new MapExample().run()
}


