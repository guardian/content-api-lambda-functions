package com.gu.crossword.crosswords

import com.gu.crossword.services.Http
import okhttp3._

import scala.util.Try


trait CrosswordClientOps {
  def upload(url: String)(id: String, data: Array[Byte]): Try[String]
}

trait HttpCrosswordClientOps extends CrosswordClientOps {
  def upload(url: String)(id: String, data: Array[Byte]): Try[String] = Try {
    val requestBody: RequestBody = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("result_format", "xml")
      .addFormDataPart("xml", id, RequestBody.create(MediaType.parse("application/xml"), data))
      .build()

    val request = new Request.Builder().url(url).post(requestBody).build()
    val response: Response = Http.httpClient.newCall(request).execute()

    if(!response.isSuccessful) {
      response.body.close()
      throw new RuntimeException(
        s"Crossword upload failed for crossword: $id, got response code: ${response.code()}"
      )
    } else {
      val responseBody = response.body().string()
      response.body.close()
      responseBody
    }
  }
}