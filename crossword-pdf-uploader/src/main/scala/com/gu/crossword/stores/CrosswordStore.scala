package com.gu.crossword.stores

import java.io.ByteArrayOutputStream

import com.amazonaws.services.s3.model.S3Object
import com.google.common.io.ByteStreams
import com.gu.crossword.models.{ CrosswordPdfFile, CrosswordPdfFileName }
import scala.collection.JavaConversions._

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

  private def getCrosswordKeys: List[String] = {
    val s3ObjectSummaries = s3Client.listObjects(bucketName).getObjectSummaries.toList
      .collect { case os if os.getKey.endsWith(".pdf") => os }

    /* Sort crosswords by name */
    val groupedSummaries = s3ObjectSummaries.groupBy(os => {
      val nameParts = os.getKey.split("\\.").toList
      List(nameParts(0), nameParts(1), nameParts(2), nameParts.last).mkString(".")
    })

    /* Remove oldest version of each crossword if multiple versions */
    val newestSummaries = groupedSummaries map {
      case (fileName, listOfS3Objects) => {
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

}
