package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordXmlFile
import com.gu.crossword.services.Http.httpClient
import com.squareup.okhttp._
import scala.xml.XML

trait CrosswordUploader extends ComposerCrosswordIntegration with XmlProcessor {

  def uploadCrossword(crosswordXmlFile: CrosswordXmlFile)(implicit config: Config): Unit = {
    val request = buildRequest(crosswordXmlFile)
    val response: Response = httpClient.newCall(request).execute()

    val responseBody = response.body.string
    if (response.isSuccessful) {
      val crosswordXmlToCreatePage = process(XML.loadString(responseBody))
      println(s"creating page for crossword ${crosswordXmlFile.key} in flex.")
      createPage(crosswordXmlFile, crosswordXmlToCreatePage)
    } else
      println(s"Crossword upload failed for crossword: ${crosswordXmlFile.key}")

  }

  private def buildRequest(crosswordXmlFile: CrosswordXmlFile)(implicit config: Config) = {
    val requestBody: RequestBody = new MultipartBuilder()
      .`type`(MultipartBuilder.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", crosswordXmlFile.key, RequestBody.create(MediaType.parse("application/xml"), crosswordXmlFile.file))
      .build()

    new Request.Builder().url(config.crosswordMicroAppUrl).post(requestBody).build()
  }

}
