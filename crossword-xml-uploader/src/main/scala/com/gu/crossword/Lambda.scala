package com.gu.crossword

import java.util.{ Map => JMap }
import com.amazonaws.services.lambda.runtime.{ RequestHandler, Context }
import com.gu.crossword.crosswords.{ ComposerCrosswordIntegration, XmlProcessor, CrosswordStore, CrosswordUploader }
import com.squareup.okhttp._
import scala.xml.XML

class Lambda
    extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordUploader
    with CrosswordStore
    with XmlProcessor
    with ComposerCrosswordIntegration {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {

    implicit val config = new Config(context)

    println("The uploading of crossword xml files has started.")

    for {
      crosswordXmlFile <- getCrosswordXmlFiles
    } yield {
      val response: Response = uploadCrossword(crosswordXmlFile)
      val responseBody = response.body().string()
      if (response.isSuccessful) {
        val crosswordXmlToCreatePage = process(XML.loadString(responseBody))
        println(s"creating page for crossword in flex.")
        createPage(crosswordXmlFile, crosswordXmlToCreatePage)
      } else
        println(s"Crossword upload failed for crossword: ${crosswordXmlFile.key}")

    }

    println("The uploading of crossword xml files has finished.")
  }

}