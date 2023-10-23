package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.xmluploader._
import com.gu.crossword.xmluploader.models.CrosswordXmlFile

import scala.util.{Failure, Success}

trait CrosswordXmlUploaderLambda
  extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordConfigRetriever
    with ComposerOps
    with CrosswordStore
    with CrosswordXmlUploader {

  private def doUpload(url: String, crosswordXmlFile: CrosswordXmlFile): Either[(String, Throwable), String] = {
    val uploadResult = for {
      xmlResponse <- uploadCrossword(url)(crosswordXmlFile)
      _ = println(xmlResponse.toString())
    } yield ()
    uploadResult match {
      case Success(_) => Right(crosswordXmlFile.key)
      case Failure(error) => Left((crosswordXmlFile.key, error))
    }
  }

  def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    val config = getConfig(context)

    println("The uploading of crossword xml files has started.")

    val crosswordXmlFiles = getCrosswordXmlFiles(config.crosswordsBucketName)
    println(s"Found ${crosswordXmlFiles.size} crossword file(s) to process")

    val (failures, successes) = crosswordXmlFiles.map { crosswordXmlFile =>
      doUpload(
        url = config.crosswordV2Url,
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

    // We want to fail the lambda if any of the uploads failed
    if (failures.nonEmpty) {
      val failedKeys = failures.map(_._1).mkString(", ")
      throw new Exception(s"Failures detected when uploading crossword xml files (${failedKeys})!")
    }
  }
}

class Lambda
    extends CrosswordXmlUploaderLambda
      with KinesisComposerOps
      with S3CrosswordStore
      with HttpCrosswordClientOps
      with S3CrosswordConfigRetriever
