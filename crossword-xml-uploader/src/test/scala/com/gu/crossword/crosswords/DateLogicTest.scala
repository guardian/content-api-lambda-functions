package com.gu.crossword.crosswords

import org.scalatest.{ FlatSpec, Matchers }

import scala.io.Source
import scala.xml.XML

class DateLogicTest extends FlatSpec with Matchers with DateLogic {

  it should "transform a date during British winter time" in {
    transformDate("20.03.2016 00:00") should be("2016-03-20T00:00:00.000+00:00")
  }

  it should "transform a date during British summer time" in {
    transformDate("30.03.2016 00:00") should be("2016-03-30T00:00:00.000+01:00")
  }

}
