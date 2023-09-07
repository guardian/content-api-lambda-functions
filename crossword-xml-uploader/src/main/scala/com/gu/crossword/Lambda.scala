package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.crosswords.Composer._
import com.gu.crossword.crosswords.CrosswordStore._
import com.gu.crossword.crosswords.CrosswordUploader._

class Lambda
    extends RequestHandler[JMap[String, Object], Unit] {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {

    implicit val config = new Config(context)

    println("The uploading of crossword xml files has started.")

    getCrosswordXmlFiles(config).foreach { crosswordXmlFile =>
      (for {
        crosswordXml <- uploadCrossword(crosswordXmlFile)
        _ <- createPage(crosswordXmlFile, crosswordXml)
      } yield ()) match {
        case Left(error) =>
          println(s"Failed to upload crossword ${crosswordXmlFile.key} with error: $error")
          archiveFailedCrosswordXMLFile(config, crosswordXmlFile.key)
        case Right(_) =>
          println(s"Successfully uploaded crossword ${crosswordXmlFile.key}")
          archiveCrosswordXMLFile(config, crosswordXmlFile.key)
      }
    }

    println("The uploading of crossword xml files has finished.")
  }

}