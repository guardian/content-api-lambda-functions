package com.gu.crossword

import com.gu.crossword.models.CrosswordXmlFile
import com.squareup.okhttp._

trait CrosswordUploader {

  private val httpClient: OkHttpClient = new OkHttpClient()

  def uploadCrossword(crosswordXmlFile: CrosswordXmlFile)(implicit config: Config): Response = {
    val request = buildRequest(crosswordXmlFile)
    httpClient.newCall(request).execute()
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
