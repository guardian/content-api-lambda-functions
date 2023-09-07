package com.gu.crossword

import com.gu.crossword.crosswords.models.CrosswordXmlFile
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.xml.{Elem, XML}

class LambdaTest extends AnyFlatSpec with Matchers {

  type PageCreator = (String, Elem) => Either[Error, Unit]
  type Uploader = (String, Array[Byte]) => Either[Throwable, String]

  trait FakeLambda extends CrosswordUploaderLambda {
    var archiveCalled = 0
    var archiveFailedCalled = 0

    def archiveCrosswordXMLFile(config: Config, awsKey: String): Unit = {
      archiveCalled += 1
    }

    def archiveFailedCrosswordXMLFile(config: Config, awsKey: String): Unit = {
      archiveFailedCalled += 1
    }
  }

  def buildFakeLambda(
                       crosswordXmlFiles: List[CrosswordXmlFile] = List.empty,
                       pageCreator: PageCreator = (_, _) => Right(()),
                       uploader: Uploader = (_, _) => Right(<crossword/>.toString()),
                     ) = {
    new FakeLambda {
      override def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile] = crosswordXmlFiles

      override def createPage(streamName: String)(key: String, xmlData: Elem): Either[Error, Unit] = pageCreator(key, xmlData)

      override def upload(url: String)(id: String, data: Array[Byte]): Either[Throwable, String] = uploader(id, data)
    }
  }

  val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
  val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)

  it should "archive correctly a successfully processed crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Right(crosswordMicroAppResponseXml.toString())
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(1)
    fakeLambda.archiveFailedCalled should be(0)
  }

  it should "archive as failure a processed crossword with xml that XmlProcessor fails to parse" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Right(<invalid-xml/>.toString())
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword with invalid" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Right("not xml at all is it?")
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword that fails to upload a crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Left(new Error("Failed to upload crossword")),
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword that fails to create a page in composer" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Right(crosswordMicroAppResponseXml.toString()),
      pageCreator = (_, _) => Left(new Error("Failed to create page in composer"))
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }
}
