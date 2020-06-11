package com.teamg.taxi.core.factory

trait IdProvider[ID] {

  def next(): ID

}
