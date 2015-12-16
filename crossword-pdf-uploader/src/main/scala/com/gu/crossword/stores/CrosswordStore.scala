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
    } yield CrosswordPdfFile(key, crosswordPdfFileName, getCrossword(key))
  }

  def getCrossword(key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(bucketName, key)
    download(obj)
  }

  private def getCrosswordKeys: List[String] = {
    s3Client.listObjects(bucketName).getObjectSummaries.toList
      .collect { case os if os.getKey.endsWith(".pdf") => os.getKey }
  }

  /* Future crosswords should be skipped */
  private def isFutureCrossword(crosswordPdfFileName: CrosswordPdfFileName): Boolean = crosswordPdfFileName.getPublicationDate.isAfterNow

  private def download(obj: S3Object) = {
    val out = new ByteArrayOutputStream(obj.getObjectMetadata.getContentLength.toInt)
    ByteStreams.copy(obj.getObjectContent, out)
    out.toByteArray
  }

}
