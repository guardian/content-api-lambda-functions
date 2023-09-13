package com.gu.crossword.xmluploader

import okhttp3._

import scala.util.Try


trait CrosswordClientOps {
  def upload(url: String)(id: String, data: Array[Byte]): Try[String]
}

trait HttpCrosswordClientOps extends CrosswordClientOps {
  lazy val httpClient: OkHttpClient = new OkHttpClient()

  def upload(url: String)(id: String, data: Array[Byte]): Try[String] = Try {
    val requestBody: RequestBody = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", id, RequestBody.create(MediaType.parse("application/xml"), data))
      .build()

    val request = new Request.Builder().url(url).post(requestBody).build()
    val response: Response = httpClient.newCall(request).execute()

    if(!response.isSuccessful) {
      response.body.close()
      throw new Exception(
        s"Crossword upload failed for crossword: $id, got response code: ${response.code()}"
      )
    } else {
      val responseBody = response.body().string()
      response.body.close()
      responseBody
    }
  }
}