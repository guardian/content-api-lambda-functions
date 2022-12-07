package com.gu.crossword.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CrosswordPdfFileNameSpec extends AnyFlatSpec with Matchers {

  it should "correctly pass a key to create a CrosswordPdfFilename object" in {
    val key = "gdn.weekend.20151128.150316.9726.pdf"
    val crosswordPdfFileName = CrosswordPdfFileName(key).get
    crosswordPdfFileName.day should be("28")
    crosswordPdfFileName.month should be("11")
    crosswordPdfFileName.year should be("2015")
    crosswordPdfFileName.`type` should be("weekend")
    crosswordPdfFileName.fileName should be("gdn.weekend.20151128.pdf")
  }

  it should "set the type to 'prize' if key contains type 'cryptic and day is Saturday" in {
    val key = "gdn.cryptic.20151024.pdf"
    val crosswordPdfFileName = CrosswordPdfFileName(key).get
    crosswordPdfFileName.`type` should be("prize")
  }

  it should "correctly handle a key in an unexpected format" in {
    val key = "crossword_report_13112015-12.19.122538.9740.pdf"
    val crosswordPdfFileName = CrosswordPdfFileName(key)
    crosswordPdfFileName should be(None)
  }

}
