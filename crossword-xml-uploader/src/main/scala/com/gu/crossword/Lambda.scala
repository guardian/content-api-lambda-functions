package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.crosswords._

trait CrosswordUploaderLambda
  extends RequestHandler[JMap[String, Object], Unit]
    with ComposerOps
    with CrosswordStore
    with CrosswordUploader

class Lambda
    extends CrosswordUploaderLambda
      with KinesisComposerOps
      with S3CrosswordStore
      with HttpCrosswordUploader {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    val config = new Config(context)

    println("The uploading of crossword xml files has started.")

    getCrosswordXmlFiles(config).foreach { crosswordXmlFile =>
      (for {
        rawXml <- uploadCrossword(config.crosswordMicroAppUrl)(crosswordXmlFile)
        crosswordXml <- XmlProcessor.process(rawXml)
        _ <- createPage(config.composerCrosswordIntegrationStreamName)(crosswordXmlFile, crosswordXml)
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