package com.gu.crossword

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.pdfuploader.models.CrosswordPdfFile
import com.gu.crossword.pdfuploader.{CrosswordConfigRetriever, CrosswordPdfStore, CrosswordPdfUploader, HttpCrosswordPdfUploader, PublicPdfStore, S3CrosswordConfigRetriever, S3CrosswordPdfStore, S3PublicPdfStore}

import scala.util.{Failure, Success, Try}

trait CrosswordPdfUploaderLambda
  extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordConfigRetriever
    with CrosswordPdfUploader
    with CrosswordPdfStore
    with PublicPdfStore {

  def doUpload(bucketName: String, fileLocation: String, uploadUrl: String, pdfFile: CrosswordPdfFile): Either[(String, Throwable), String] = {
    val uploadResult = for {
      _ <- uploadPdfCrosswordFile(bucketName, fileLocation, pdfFile)
      uploadLocation = s"${fileLocation}/${pdfFile.awsKey}"
      _ <- uploadPdfCrosswordLocation(uploadUrl, pdfFile, uploadLocation)
    } yield ()

    uploadResult match {
      case Success(_) => Right(pdfFile.awsKey)
      case Failure(error) => Left((pdfFile.awsKey, error))
    }
  }

  // Upload to old crossword service - do NOT createPage
  // This should be removed once the crosswordv2 service has been running for a while
  // Wrapping with Try as we must fail safe and not stop the lambda from running if this fails
  private def doV1Upload(url: String, pdfFile: CrosswordPdfFile, fileLocation: String): Unit = Try {
    val uploadLocation = s"${fileLocation}/${pdfFile.awsKey}"
    val result = for {
      _ <- uploadPdfCrosswordLocation(url, pdfFile, uploadLocation)
    } yield ()

    result match {
      case Success(_) =>
        println(s"Successfully dual uploaded crossword PDF ${pdfFile.awsKey} to old crossword service")
      case Failure(error) =>
        println(
          s"Failed to dual upload crossword PDF ${pdfFile.awsKey} to old crossword service with error: ${error.getMessage}"
        )
        error.getStackTrace.foreach(println)
    }
  }

  def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    val config = getConfig(context)

    println("The uploading of crossword PDF files has started.")
    val crosswordPdfFiles = getCrosswordPdfFiles(config.crosswordsBucketName)
    println(s"Found ${crosswordPdfFiles.size} crossword PDF file(s) to process")

    val (failures, successes) = crosswordPdfFiles.map { pdfFile =>
      doUpload(
        bucketName = config.crosswordPdfPublicBucketName,
        fileLocation = config.crosswordPdfPublicFileLocation,
        uploadUrl = config.crosswordV2Url,
        pdfFile = pdfFile
      )
    } partitionMap (identity)

    failures.foreach {
      case (key, e) =>
        println(s"Failed to upload crossword PDF ${key} with error: ${e.getMessage}")
        e.printStackTrace()
        archiveFailedPdfFiles(config.crosswordsBucketName, key)
    }

    successes.foreach { key =>
      println(s"Successfully uploaded crossword PDF: ${key}")
      archiveProcessedPdfFiles(config.crosswordsBucketName, key)
    }

    println(s"The uploading of crossword PDF files has finished, ${successes.size} succeeded, ${failures.size} failed.}")

    // Dual upload to old crossword service
    config.crosswordMicroAppUrl.map(url =>
      crosswordPdfFiles.map(doV1Upload(url, _, config.crosswordPdfPublicFileLocation))
    )

    // We want to fail the lambda if any of the uploads failed
    if (failures.size > 0) {
      val failedKeys = failures.map(_._1).mkString(", ")
      throw new Exception(s"Failures detected when uploading crossword PDF files (${failedKeys})!")
    }

    println("The uploading of crossword PDF files has finished.")
  }
}

class Lambda
  extends CrosswordPdfUploaderLambda
    with S3CrosswordConfigRetriever
    with HttpCrosswordPdfUploader
    with S3CrosswordPdfStore
    with S3PublicPdfStore

