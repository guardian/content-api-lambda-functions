package com.gu.crossword.stores

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

import com.amazonaws.services.s3.model.{ S3ObjectSummary, S3Object }
import com.google.common.io.ByteStreams
import com.gu.crossword.models.{ CrosswordPdfFile, CrosswordPdfFileName }

trait CrosswordStore extends S3Provider {

  private val processingBucketName = "crossword-files-for-processing"

  def getCrosswordPdfFiles: List[CrosswordPdfFile] = {
    for {
      key <- getCrosswordKeys
      crosswordPdfFileName <- CrosswordPdfFileName(key)
      if !isFutureCrossword(crosswordPdfFileName)
    } yield CrosswordPdfFile(crosswordPdfFileName.fileName, crosswordPdfFileName, getCrossword(key))
  }

  def getCrossword(key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(processingBucketName, key)
    download(obj)
  }

  private def getCrosswordPdfObjectSummaries: List[S3ObjectSummary] = {
    s3Client.listObjects(processingBucketName).getObjectSummaries.asScala.toList
      .collect { case os if os.getKey.endsWith(".pdf") => os }
  }

  private def getCrosswordKeys: List[String] = {

    /* Sort crosswords by name */
    val groupedSummaries = getCrosswordPdfObjectSummaries.groupBy(os => {
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

  private def moveFileToS3Bucket(awsKey: String, bucketName: String): Unit = {
    println(s"Moving $awsKey to bucket $bucketName")
    s3Client.copyObject(processingBucketName, awsKey, bucketName, awsKey)
    s3Client.deleteObject(processingBucketName, awsKey)
  }

  def archiveProcessedPdfFiles(awsKey: String): Unit = {
    println(s"Archiving all versions of $awsKey")
    val fileName = awsKey.dropRight(4) // remove .pdf file extension
    val keysToArchive = getCrosswordPdfObjectSummaries.filter(_.getKey.startsWith(fileName)).map(_.getKey)
    keysToArchive.foreach(key => moveFileToS3Bucket(key, "crossword-processed-files"))
  }

  def archiveFailedPdfFiles(awsKey: String): Unit = {
    println(s"Moving all versions of $awsKey to failed bucket")
    val fileName = awsKey.dropRight(4)
    val keysToArchive = getCrosswordPdfObjectSummaries.filter(_.getKey.startsWith(fileName)).map(_.getKey)
    keysToArchive.foreach(key => moveFileToS3Bucket(key, "crossword-failed-files"))
  }
}
