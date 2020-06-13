package com.teamg.taxi.core.utils

import scala.util.Random

object Utils {
  def getRandomElement[A](seq: Seq[A], random: Random): A =
    seq(random.nextInt(seq.length))
}
