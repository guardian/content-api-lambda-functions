package com.gu.crossword.pdfuploader

import com.amazonaws.services.s3.model.{S3Object, S3ObjectSummary}
import com.google.common.io.ByteStreams
import com.gu.crossword.pdfuploader.models.{CrosswordPdfFile, CrosswordPdfFileName}
import com.gu.crossword.services.AWS.s3Client

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

trait CrosswordPdfStore {
  def getCrosswordPdfFiles(bucketName: String): List[CrosswordPdfFile]
  def archiveProcessedPdfFiles(bucketName: String, key: String): Unit
  def archiveFailedPdfFiles(bucketName: String, key: String): Unit
}

trait S3CrosswordPdfStore extends CrosswordPdfStore {

  def getCrosswordPdfFiles(bucketName: String): List[CrosswordPdfFile] = {
    for {
      key <- getCrosswordKeys(bucketName)
      crosswordPdfFileName <- CrosswordPdfFileName(key)
      if !isFutureCrossword(crosswordPdfFileName)
    } yield CrosswordPdfFile(
      awsKey = crosswordPdfFileName.fileName,
      filename = crosswordPdfFileName,
      file = getObject(bucketName, key)
    )
  }

  private def getObject(bucketName: String, key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(bucketName, key)
    download(obj)
  }

  private def getCrosswordPdfObjectSummaries(bucketName: String): List[S3ObjectSummary] = {
    s3Client.listObjects(bucketName).getObjectSummaries.asScala.toList
      .collect { case os if os.getKey.endsWith(".pdf") => os }
  }

  private def getCrosswordKeys(bucketName: String): List[String] = {

    /* Sort crosswords by name */
    val groupedSummaries = getCrosswordPdfObjectSummaries(bucketName).groupBy(os => {
      val nameParts = os.getKey.split("\\.").toList
      if (nameParts.length >= 3) {
        List(nameParts(0), nameParts(1), nameParts(2)).mkString(".")
      } else os.getKey
    })

    /* Remove oldest version of each crossword if multiple versions */
    val newestSummaries = groupedSummaries map {
      case (_, listOfS3Objects) => {
        listOfS3Objects.sortWith(_.getLastModified.getTime > _.getLastModified.getTime).head.getKey
      }
    }

    newestSummaries.toList
  }

  /* Future crosswords should be skipped */
  private def isFutureCrossword(crosswordPdfFileName: CrosswordPdfFileName): Boolean = crosswordPdfFileName.getPublicationDate.isAfterNow

  private def download(obj: S3Object) = {
    val out = new ByteArrayOutputStream(obj.getObjectMetadata.getContentLength.toInt)
    ByteStreams.copy(obj.getObjectContent, out)
    out.toByteArray
  }

  private def moveFileToS3Bucket(sourceBucketName: String, targetBucketName: String, key: String): Unit = {
    println(s"Moving $key to bucket $targetBucketName")
    s3Client.copyObject(sourceBucketName, key, targetBucketName, key)
    s3Client.deleteObject(sourceBucketName, key)
  }

  def archiveProcessedPdfFiles(bucketName: String, key: String): Unit = {
    val archiveBucketName = "crossword-processed-files"

    println(s"Archiving all versions of $key")
    val fileName = key.dropRight(4) // remove .pdf file extension
    val keysToArchive = getCrosswordPdfObjectSummaries(bucketName).filter(_.getKey.startsWith(fileName)).map(_.getKey)
    keysToArchive.foreach(key => moveFileToS3Bucket(bucketName, archiveBucketName, key))
  }

  def archiveFailedPdfFiles(bucketName: String, key: String): Unit = {
    val processingFailedBucketName = "crossword-failed-files"

    println(s"Moving all versions of $key to failed bucket")
    val fileName = key.dropRight(4) // remove .pdf file extension
    val keysToArchive = getCrosswordPdfObjectSummaries(bucketName)
      .filter(_.getKey.startsWith(fileName))
      .map(_.getKey)
    keysToArchive.foreach(key =>
      moveFileToS3Bucket(bucketName, processingFailedBucketName, key)
    )
  }
}
