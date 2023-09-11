package com.gu.crossword

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.crosswords.HttpCrosswordClientOps
import com.gu.crossword.crosswords.models.{CrosswordLambdaConfig, CrosswordXmlFile}
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import okhttp3.mockwebserver.{MockResponse, MockWebServer}

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}


class LambdaTest extends AnyFlatSpec with Matchers with TryValues {

  type PageCreator = (String, Elem) => Try[Unit]
  type Uploader = (String, Array[Byte]) => Try[String]

  trait FakeLambda extends CrosswordUploaderLambda {
    var archiveCalled = 0
    var archiveFailedCalled = 0

    def archiveCrosswordXMLFile(bucketName: String, awsKey: String): Unit = {
      archiveCalled += 1
    }

    def archiveFailedCrosswordXMLFile(bucketName: String, awsKey: String): Unit = {
      archiveFailedCalled += 1
    }
  }

  def buildFakeLambda(
                       crosswordXmlFiles: List[CrosswordXmlFile] = List.empty,
                       pageCreator: PageCreator = (_, _) => Success(()),
                       uploader: Uploader = (_, _) => Success(<crossword/>.toString()),
                     ) = {
    new FakeLambda {
      override def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile] = crosswordXmlFiles

      override def createPage(streamName: String)(key: String, xmlData: Elem): Try[Unit] = pageCreator(key, xmlData)

      override def upload(url: String)(id: String, data: Array[Byte]): Try[String] = uploader(id, data)

      override def getConfig(context: Context): CrosswordLambdaConfig = CrosswordLambdaConfig(
        crosswordsBucketName = "crosswords-bucket",
        crosswordMicroAppUrl = "https://crossword-microapp-url",
        crosswordV2Url = None,
        composerCrosswordIntegrationStreamName = "crossword-integration-stream-name",
      )
    }
  }

  val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
  val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)

  it should "archive correctly a successfully processed crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success(crosswordMicroAppResponseXml.toString())
    )

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(1)
    fakeLambda.archiveFailedCalled should be(0)
  }

  it should "archive as failure a processed crossword with xml that XmlProcessor fails to parse" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success(<invalid-xml/>.toString())
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword with invalid" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success("not xml at all is it?")
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword that fails to upload a crossword" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Failure(new Error("Failed to upload crossword")),
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword that fails to create a page in composer" in {
    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)
    val fakeLambda = buildFakeLambda(
      crosswordXmlFiles = List(crosswordXmlFile),
      uploader = (_, _) => Success(crosswordMicroAppResponseXml.toString()),
      pageCreator = (_, _) => Failure(new Error("Failed to create page in composer"))
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include("Failures detected when uploading crossword xml files (key)!")

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "not fail if the v2 endpoint fails" in {
    val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
    val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)

    val expectedResponse = crosswordMicroAppResponseXml.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/upload").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse));

    val crosswordXmlFile = CrosswordXmlFile("key", Array.empty)

    val fakeLambda = new FakeLambda with HttpCrosswordClientOps {
      override def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile] = List(crosswordXmlFile)
      override def createPage(streamName: String)(key: String, xmlData: Elem): Try[Unit] = Success(())

      override def getConfig(context: Context): CrosswordLambdaConfig = CrosswordLambdaConfig(
        crosswordsBucketName = "crosswords-bucket",
        crosswordMicroAppUrl = baseUrl,
        crosswordV2Url = Some("https://crossword-v2-url"),
        composerCrosswordIntegrationStreamName = "crossword-integration-stream-name",
      )
    }

    fakeLambda.handleRequest(null, null)

    fakeLambda.archiveCalled should be(1)
    fakeLambda.archiveFailedCalled should be(0)

    mockHttpServer.shutdown()
  }
}
