package com.gu.crossword.crosswords

import okhttp3.mockwebserver.MockResponse
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HttpCrosswordClientOpsTest extends AnyFlatSpec with Matchers with TryValues {

  import okhttp3.mockwebserver.MockWebServer

  val httpCrosswordClientOps = new HttpCrosswordClientOps() {}

  behavior of "HttpCrosswordClientOpsTest"

  it should "return the response" in {
    val expectedResponse = <response />.toString()

    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()

    val baseUrl = mockHttpServer.url("/upload").toString
    mockHttpServer.enqueue(new MockResponse().setBody(expectedResponse));

    val result = httpCrosswordClientOps.upload(baseUrl)("id", Array.empty)
    result.get shouldBe expectedResponse

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
    result.failed.get.getMessage should include ("timeout")

    mockHttpServer.shutdown()
  }
}
