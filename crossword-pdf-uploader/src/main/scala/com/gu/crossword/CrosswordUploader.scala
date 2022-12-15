package com.gu.crossword

import com.gu.crossword.models.{ CrosswordPdfFile, CrosswordPdfFileName }
import okhttp3._

trait CrosswordUploader {

  private val httpClient: OkHttpClient = new OkHttpClient()

  def uploadPdfCrosswordLocation(crosswordPdfFile: CrosswordPdfFile, location: String)(implicit config: Config): Response = {
    val request = buildRequest(crosswordPdfFile.filename, location)
    httpClient.newCall(request).execute()
  }

  private def buildRequest(crosswordPdfFileName: CrosswordPdfFileName, location: String)(implicit config: Config) = {

    val requestBody: RequestBody = new FormBody.Builder()
      .add("type", crosswordPdfFileName.`type`)
      .add("year", crosswordPdfFileName.year)
      .add("month", crosswordPdfFileName.month)
      .add("day", crosswordPdfFileName.day)
      .add("pdf", location) //location of file in s3
      .build()

    new Request.Builder().url(config.crosswordMicroAppUrl).post(requestBody).build()
  }

}
