package com.gu.crossword

import com.squareup.okhttp._

trait CrosswordUploader {

  private val httpClient: OkHttpClient = new OkHttpClient()

  def uploadCrossword(crossword: Array[Byte])(implicit config: Config): Response = {
    val request = buildRequest(crossword)
    httpClient.newCall(request).execute()
  }

  private def buildRequest(fileContents: Array[Byte])(implicit config: Config) = {
    val requestBody: RequestBody = new MultipartBuilder()
      .`type`(MultipartBuilder.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", null, RequestBody.create(MediaType.parse("application/xml"), fileContents))
      .build()

    new Request.Builder().url(config.crosswordMicroAppUrl).post(requestBody).build()
  }

}
