package com.gu.crossword

import java.util.{ Map => JMap }
import com.amazonaws.services.lambda.runtime.{ RequestHandler, Context }
import com.gu.crossword.crosswords.{ CrosswordStore, CrosswordUploader }

class Lambda
    extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordUploader
    with CrosswordStore {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {

    implicit val config = new Config(context)

    println("The uploading of crossword xml files has started.")

    for {
      crosswordXmlFile <- getCrosswordXmlFiles(config)
    } yield {
      uploadCrossword(crosswordXmlFile)
    }

    println("The uploading of crossword xml files has finished.")

    println("Archiving non-crossword files...")

    for (nonCrosswordFileKey <- getNotCrosswordFileKeys(config)) {
      println(s"Archiving $nonCrosswordFileKey")
      archiveFailedCrosswordXMLFile(config, nonCrosswordFileKey)
    }

    println("Archiving complete")
  }
}
