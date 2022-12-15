package com.gu.crossword.crosswords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DateLogicTest extends AnyFlatSpec with Matchers with DateLogic {

  it should "transform a date during British winter time" in {
    transformDate("20.03.2016 00:00") should be("2016-03-20T00:00:00.000+00:00")
  }

  it should "transform a date during British summer time" in {
    transformDate("30.03.2016 00:00") should be("2016-03-29T23:00:00.000+00:00")
  }

}
