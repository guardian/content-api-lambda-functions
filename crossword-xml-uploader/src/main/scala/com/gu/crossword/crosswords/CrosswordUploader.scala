package com.gu.crossword.crosswords

import com.gu.crossword.crosswords.models._
import com.gu.crossword.services.Http.httpClient

import scala.xml.{Elem, XML}
import okhttp3._

import scala.util.Try

trait CrosswordUploader {
  def uploadCrossword(crosswordMicroAppUrl: String)(crosswordXmlFile: CrosswordXmlFile): Either[Throwable, Elem]
}

trait HttpCrosswordUploader extends CrosswordUploader {
  def uploadCrossword(crosswordMicroAppUrl: String)(crosswordXmlFile: CrosswordXmlFile): Either[Throwable, Elem] = {
    val requestBody: RequestBody = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", crosswordXmlFile.key, RequestBody.create(MediaType.parse("application/xml"), crosswordXmlFile.file))
      .build()

    val request = new Request.Builder().url(crosswordMicroAppUrl).post(requestBody).build()

    val response: Response = httpClient.newCall(request).execute()

    val responseBody = response.body.string

    if (!response.isSuccessful) {
      Left(new Error(s"Crossword upload failed for crossword: ${crosswordXmlFile.key}"))
    } else {
      Try {
        XML.loadString(responseBody)
      }.toEither
    }
  }
}