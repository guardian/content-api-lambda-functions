package com.gu.crossword.xmluploader

import okhttp3.MultipartReader
import okhttp3.mockwebserver.MockResponse
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import okhttp3.mockwebserver.MockWebServer

import java.nio.charset.StandardCharsets


class HttpCrosswordClientOpsTest extends AnyFlatSpec with Matchers with TryValues {


  val httpCrosswordClientOps = new HttpCrosswordClientOps() {}

  behavior of "HttpCrosswordClientOpsTest"

  it should "return the response" in {
    val expectedResponse = <response />.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/upload").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse));

    val encodedString = "hello world"
    val sentData = encodedString.getBytes(StandardCharsets.UTF_8);
    val result = httpCrosswordClientOps.upload(baseUrl)("id", sentData)
    result.get shouldBe expectedResponse

    val request = mockHttpServer.takeRequest();
    request.getPath should be("/upload")
    request.getMethod should be("POST")
    request.getHeader("Content-Type") should include("multipart/form-data; boundary=")

    // parse multipart/form-data body
    val boundary = request.getHeader("Content-Type").split("boundary=").last
    val multipartReader = new MultipartReader(request.getBody, boundary)

    val resultFormatPart = multipartReader.nextPart()
    resultFormatPart.headers().get("Content-Disposition") should include("name=\"result_format\"")
    resultFormatPart.body().readString(StandardCharsets.UTF_8) shouldBe "xml"

    val fileData = multipartReader.nextPart()
    fileData.headers().get("Content-Disposition") should include("name=\"xml\"; filename=\"id")
    fileData.body().readString(StandardCharsets.UTF_8) shouldBe encodedString

    multipartReader.nextPart() shouldBe null

    mockHttpServer.shutdown()
  }

  it should "return failure on a non-200 response" in {
    val expectedResponse = <failure/>.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/upload").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse).setStatus("HTTP/1.1 500 Internal Server Error"));

    val result = httpCrosswordClientOps.upload(baseUrl)("id", Array.empty)
    result.failed.get.getMessage should include ("got response code: 500")

    mockHttpServer.shutdown()
  }

  it should "return failure on a timeout" in {
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("/upload").toString

    val result = httpCrosswordClientOps.upload(baseUrl)("id", Array.empty)
    result.failed.get.getMessage should include ("time")

    mockHttpServer.shutdown()
  }
}
