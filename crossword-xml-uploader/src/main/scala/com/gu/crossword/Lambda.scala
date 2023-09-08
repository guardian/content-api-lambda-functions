package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.xmluploader._
import com.gu.crossword.xmluploader.models.CrosswordXmlFile

import scala.util.{Failure, Success, Try}

trait CrosswordXmlUploaderLambda
  extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordConfigRetriever
    with ComposerOps
    with CrosswordStore
    with CrosswordXmlUploader {

  private def doUpload(url: String, streamName: String, crosswordXmlFile: CrosswordXmlFile): Either[(String, Throwable), String] = {
    val uploadResult = for {
      rawXml <- uploadCrossword(url)(crosswordXmlFile)
      crosswordXml <- XmlProcessor.process(rawXml)
      _ <- createPage(streamName)(crosswordXmlFile.key, crosswordXml)
    } yield ()
    uploadResult match {
      case Success(_) => Right(crosswordXmlFile.key)
      case Failure(error) => Left((crosswordXmlFile.key, error))
    }
  }

  // Upload to crosswordv2 service - do NOT createPage
  // This should be removed once the crosswordv2 service is ready to be used
  // Wrapping with Try as we must fail safe and not stop the lambda from running if this fails
  private def doV2Upload(url: String, crosswordXmlFile: CrosswordXmlFile): Unit = Try {
    println(s"Attempting to dual upload crossword ${crosswordXmlFile.key} to crosswordv2")
    val v2Result = for {
      rawXml <- uploadCrossword(url)(crosswordXmlFile)
      _ <- XmlProcessor.process(rawXml)
    } yield ()

    v2Result match {
      case Success(_) =>
        println(s"Successfully dual uploaded crossword ${crosswordXmlFile.key} to crosswordv2")
      case Failure(e) =>
        println(
          s"Failed to dual upload crossword ${crosswordXmlFile.key} to crosswordv2 with error: ${e.getMessage}"
        )
        e.printStackTrace()
    }
  }

  def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    val config = getConfig(context)

    println("The uploading of crossword xml files has started.")

    val crosswordXmlFiles = getCrosswordXmlFiles(config.crosswordsBucketName)
    println(s"Found ${crosswordXmlFiles.size} crossword file(s) to process")

    val (failures, successes) = crosswordXmlFiles.map { crosswordXmlFile =>
      doUpload(
        url = config.crosswordMicroAppUrl,
        streamName = config.composerCrosswordIntegrationStreamName,
        crosswordXmlFile = crosswordXmlFile
      )
    } partitionMap (identity)

    failures.foreach {
      case (key, e) =>
        println(s"Failed to upload crossword ${key} with error: ${e.getMessage}")
        e.printStackTrace()
        archiveFailedCrosswordXMLFile(config.crosswordsBucketName, key)
    }

    successes.foreach { key =>
      println(s"Successfully uploaded crossword ${key}")
      archiveCrosswordXMLFile(config.crosswordsBucketName, key)
    }

    println(s"The uploading of crossword xml files has finished, ${successes.size} succeeded, ${failures.size} failed.}")

    // Dual upload to crosswordv2 service if config present
    config.crosswordV2Url.map(url =>
      crosswordXmlFiles.map(doV2Upload(url, _))
    )

    // We want to fail the lambda if any of the uploads failed
    if (failures.size > 0) {
      val failedKeys = failures.map(_._1).mkString(", ")
      throw new Exception(s"Failures detected when uploading crossword xml files (${failedKeys})!")
    }
  }
}

class Lambda
<<<<<<< HEAD
  extends CrosswordXmlUploaderLambda
    with KinesisComposerOps
    with S3CrosswordStore
    with CrosswordXmlUploader
    with HttpCrosswordClientOps
    with S3CrosswordConfigRetriever

=======
    extends CrosswordXmlUploaderLambda
      with KinesisComposerOps
      with S3CrosswordStore
      with CrosswordXmlUploader
      with HttpCrosswordClientOps
      with S3CrosswordConfigRetriever
>>>>>>> fcb63f7 (Update pdf uploader)
