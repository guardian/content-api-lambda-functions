package com.gu.crossword

import java.util.{ Map => JMap }
import com.amazonaws.services.lambda.runtime.{ RequestHandler, Context }
import com.squareup.okhttp._

class Lambda
    extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordUploader
    with CrosswordStore {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {

    implicit val config = new Config(context)

    println("The uploading of crossword xml files has started.")

    for {
      crosswordXmlFile <- getCrosswordXmlFiles
    } yield {
      val response: Response = uploadCrossword(crosswordXmlFile)
      val responseBody = response.body().string()
      if (response.isSuccessful) {
        /* TODO: Create page given flex integration endpoint - faking for now */
        println(s"creating page for crossword in flex with request body: $responseBody")
      } else {
        println(s"Crossword upload failed for crossword: ${crosswordXmlFile.key}")
      }
    }

    println("The uploading of crossword xml files has finished.")
  }

}