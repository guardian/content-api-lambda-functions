package com.gu.crossword.xmluploader


import scala.io.Source
import scala.xml.XML
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class XmlProcessorTest extends AnyFlatSpec with Matchers {
  import XmlProcessor._

  behavior of "XmlProcessorTest - should correctly transform crossword microapp xml to flex integration xml."

  val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
  val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)
  val processedXml = process(crosswordMicroAppResponseXml).getOrElse(throw new Exception("Failed to process xml."))

  it should "process the notes correctly." in {
    (processedXml \\ "crossword").head.attribute("notes").get.text should be("Notes for crossword article")
  }

  it should "process the cms path correctly." in {
    (processedXml \\ "crossword").head.attribute("cms-path").get.text should be("/Guardian/crosswords/quiptic/834")
  }

  it should "process the publication correctly." in {
    (processedXml \\ "publication").text should be("guardian.co.uk publication")
  }

  it should "process the issue date correctly." in {
    (processedXml \\ "crossword").head.attribute("issue-date").get.text should be("2015-11-09T00:00:00.000+00:00")
  }

  it should "process the web publication date correctly." in {
    (processedXml \\ "crossword").head.attribute("web-publication-date").get.text should be("2015-11-09T00:00:00.000+00:00")
  }

  it should "process the headline correctly." in {
    (processedXml \\ "headline").text should be("Quiptic crossword No 834")
  }

  it should "process the linktext correctly." in {
    (processedXml \\ "linktext").text should be("Quiptic crossword No 834")
  }

  it should "process the trailtext correctly." in {
    (processedXml \\ "trail").text should be("Quiptic crossword No 834")
  }

  it should "process commentable correctly." in {
    (processedXml \\ "crossword").head.attribute("enable-comments").get.text should be("true")
  }

  it should "process the external references correctly." in {
    val references = (processedXml \\ "externalReference").toSeq

    references(0).attribute("type").get.text should be("CROSSWORD")
    references(0).attribute("token").get.text should be("Crossword")

    references(1).attribute("type").get.text should be("CROSSWORD")
    references(1).attribute("token").get.text should be("Quiptic")

    references(2).attribute("type").get.text should be("CROSSWORD")
    references(2).attribute("token").get.text should be("Moley")
  }

}
