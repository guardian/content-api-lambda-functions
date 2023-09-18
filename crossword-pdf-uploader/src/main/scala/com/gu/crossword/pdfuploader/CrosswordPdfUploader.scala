package com.gu.crossword.pdfuploader

import com.gu.crossword.pdfuploader.models.CrosswordPdfFile
import okhttp3._
import org.apache.http.HttpStatus

import scala.util.Try

trait CrosswordPdfUploader {
  def uploadPdfCrosswordLocation(url: String, crosswordPdfFile: CrosswordPdfFile, location: String): Try[Unit]
}

trait HttpCrosswordPdfUploader extends CrosswordPdfUploader {

  private val httpClient: OkHttpClient = new OkHttpClient()

  def uploadPdfCrosswordLocation(url: String, crosswordPdfFile: CrosswordPdfFile, location: String): Try[Unit] = Try {
    val requestBody: RequestBody = new FormBody.Builder()
      .add("type", crosswordPdfFile.filename.`type`)
      .add("year", crosswordPdfFile.filename.year)
      .add("month", crosswordPdfFile.filename.month)
      .add("day", crosswordPdfFile.filename.day)
      .add("pdf", location) //location of file in s3
      .build()

    val request = new Request.Builder().url(url).post(requestBody).build()

    val response = httpClient.newCall(request).execute()
    response.close()

    // Fail if the crossword microapp returns a 404, as this means the crossword XML file has not been uploaded yet.
    if (response.code() == HttpStatus.SC_NOT_FOUND) {
      throw new Exception(
        s"Could not find crossword for PDF: ${crosswordPdfFile.awsKey}!\n" +
          "Ensure the relevant crossword XML file has been uploaded first.")
    }

    // Fail for any non-200 response code.
    if(!response.isSuccessful) {
      throw new Exception(
        s"Crossword PDF upload failed: ${crosswordPdfFile.filename}, got response code: ${response.code()}"
      )
    }
  }
}
