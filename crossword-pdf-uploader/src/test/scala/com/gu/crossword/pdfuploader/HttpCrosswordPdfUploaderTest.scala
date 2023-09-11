package com.gu.crossword.pdfuploader

import com.gu.crossword.pdfuploader.models.{CrosswordPdfFile, CrosswordPdfFileName}
import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class HttpCrosswordPdfUploaderTest extends AnyFlatSpec with Matchers with TryValues {

  val httpCrosswordPdfUploader = new HttpCrosswordPdfUploader() {}

  behavior of "HttpCrosswordPdfUploaderTest"

  it should "succeed if the response is 200" in {
    val expectedResponse = <response />.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/pdf").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse));

    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val result = httpCrosswordPdfUploader.uploadPdfCrosswordLocation(
      url = baseUrl,
      CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty),
      location = "https://crosswords"
    )
    result shouldBe a[Success[_]]

    mockHttpServer.shutdown()
  }

  it should "return failure on a non-200 response" in {
    val expectedResponse = <failure/>.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/pdf").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse).setStatus("HTTP/1.1 500 Internal Server Error"));

    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val result = httpCrosswordPdfUploader.uploadPdfCrosswordLocation(
      url = baseUrl,
      CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty),
      location = "https://crosswords"
    )
    result.failed.get.getMessage should include ("got response code: 500")

    mockHttpServer.shutdown()
  }

  it should "return failure on a timeout" in {
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/pdf").toString

    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val result = httpCrosswordPdfUploader.uploadPdfCrosswordLocation(
      url = baseUrl,
      CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty),
      location = "https://crosswords"
    )
    result.failed.get.getMessage should include("time")

    mockHttpServer.shutdown()
  }
}
