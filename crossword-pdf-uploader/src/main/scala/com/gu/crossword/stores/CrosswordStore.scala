package com.gu.crossword.stores

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

import com.amazonaws.services.s3.model.{ S3ObjectSummary, S3Object }
import com.google.common.io.ByteStreams
import com.gu.crossword.models.{ CrosswordPdfFile, CrosswordPdfFileName }

trait CrosswordStore extends S3Provider {

  private val bucketName = "crossword-files-for-processing"

  def getCrosswordPdfFiles: List[CrosswordPdfFile] = {
    for {
      key <- getCrosswordKeys
      crosswordPdfFileName <- CrosswordPdfFileName(key)
      if !isFutureCrossword(crosswordPdfFileName)
    } yield CrosswordPdfFile(crosswordPdfFileName.fileName, crosswordPdfFileName, getCrossword(key))
  }

  def getCrossword(key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(bucketName, key)
    download(obj)
  }

  private def getCrosswordPdfObjectSummaries: List[S3ObjectSummary] = {
    s3Client.listObjects(bucketName).getObjectSummaries.asScala.toList
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

  private def archiveCrosswordPdfFile(awsKey: String) = {
    val archiveBucketName = "crossword-processed-files"
    println(s"Moving $awsKey to bucket $archiveBucketName")
    s3Client.copyObject(bucketName, awsKey, archiveBucketName, awsKey)
    s3Client.deleteObject(bucketName, awsKey)
  }

  def archiveProcessedPdfFiles(uploadedCrosswordPdfKey: String): Unit = {
    println(s"Archiving all versions of $uploadedCrosswordPdfKey")
    val uploadedCrosswordPdfKeyPrefix = uploadedCrosswordPdfKey.dropRight(4) // remove .pdf file extension
    val keysToArchive = getCrosswordPdfObjectSummaries.filter(_.getKey.startsWith(uploadedCrosswordPdfKeyPrefix)).map(_.getKey)
    keysToArchive.foreach(archiveCrosswordPdfFile)
  }

}
