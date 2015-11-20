package com.gu.crossword

import com.gu.crossword.models.{ CrosswordPdfFile, CrosswordPdfFileName }
import com.squareup.okhttp._

trait CrosswordUploader {

  private val httpClient: OkHttpClient = new OkHttpClient()

  def uploadPdfCrosswordLocation(crosswordPdfFile: CrosswordPdfFile, location: String)(implicit config: Config): Response = {
    val request = buildRequest(crosswordPdfFile.filename, location)
    httpClient.newCall(request).execute()
  }

  private def buildRequest(crosswordPdfFileName: CrosswordPdfFileName, location: String)(implicit config: Config) = {

    val requestBody: RequestBody = new MultipartBuilder()
      .`type`(MultipartBuilder.FORM)
      .addFormDataPart("type", crosswordPdfFileName.`type`)
      .addFormDataPart("year", crosswordPdfFileName.year)
      .addFormDataPart("month", crosswordPdfFileName.month)
      .addFormDataPart("day", crosswordPdfFileName.day)
      .addFormDataPart("pdf", location) //location of file in s3
      .build()

    new Request.Builder().url(config.crosswordMicroAppUrl).post(requestBody).build()
  }

}
