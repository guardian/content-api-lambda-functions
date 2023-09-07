package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models._
import com.gu.crossword.services.Http.httpClient

import scala.xml.{Elem, XML}
import okhttp3._

trait CrosswordUploader extends ComposerCrosswordIntegration with XmlProcessor {

  private def buildRequest(crosswordXmlFile: CrosswordXmlFile)(implicit config: Config) = {
    val requestBody: RequestBody = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", crosswordXmlFile.key, RequestBody.create(MediaType.parse("application/xml"), crosswordXmlFile.file))
      .build()

    new Request.Builder().url(config.crosswordMicroAppUrl).post(requestBody).build()
  }

  def uploadCrossword(crosswordXmlFile: CrosswordXmlFile)(implicit config: Config): Either[Error, Elem] = {
    val request = buildRequest(crosswordXmlFile)
    val response: Response = httpClient.newCall(request).execute()

    val responseBody = response.body.string

    if (!response.isSuccessful) {
      Left(new Error(s"Crossword upload failed for crossword: ${crosswordXmlFile.key}"))
    } else {
      Right(process(XML.loadString(responseBody)))
    }
  }
}