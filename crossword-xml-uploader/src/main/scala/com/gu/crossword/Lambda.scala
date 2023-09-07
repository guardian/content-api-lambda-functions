package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.crosswords._

trait CrosswordUploaderLambda
  extends RequestHandler[JMap[String, Object], Unit]
    with ComposerOps
    with CrosswordStore
    with CrosswordConfigRetriever
    with CrosswordUploader {
  def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    val config = getConfig(context)

    println("The uploading of crossword xml files has started.")

    getCrosswordXmlFiles(config.crosswordsBucketName).foreach { crosswordXmlFile =>
      (for {
        rawXml <- uploadCrossword(config.crosswordMicroAppUrl)(crosswordXmlFile)
        crosswordXml <- XmlProcessor.process(rawXml)
        _ <- createPage(config.composerCrosswordIntegrationStreamName)(crosswordXmlFile.key, crosswordXml)
      } yield ()) match {
        case Left(error) =>
          println(s"Failed to upload crossword ${crosswordXmlFile.key} with error: ${error.getMessage}")
          error.getStackTrace.foreach(println)
          archiveFailedCrosswordXMLFile(config.crosswordsBucketName, crosswordXmlFile.key)
        case Right(_) =>
          println(s"Successfully uploaded crossword ${crosswordXmlFile.key}")
          archiveCrosswordXMLFile(config.crosswordsBucketName, crosswordXmlFile.key)
      }
    }

    println("The uploading of crossword xml files has finished.")
  }
}

class Lambda
    extends CrosswordUploaderLambda
      with KinesisComposerOps
      with S3CrosswordStore
      with CrosswordUploader
      with HttpCrosswordClientOps
      with S3CrosswordConfigRetriever