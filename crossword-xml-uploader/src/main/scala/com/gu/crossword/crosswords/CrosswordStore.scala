package com.gu.crossword.crosswords

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

import com.amazonaws.services.s3.model.S3Object
import com.google.common.io.ByteStreams
import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordXmlFile
import com.gu.crossword.services.AWS.s3Client

trait CrosswordStore {
  def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile]
  def archiveCrosswordXMLFile(config: Config, awsKey: String): Unit
  def archiveFailedCrosswordXMLFile(config: Config, awsKey: String): Unit
}

trait S3CrosswordStore extends CrosswordStore {
  def getCrosswordXmlFiles(crosswordsBucketName: String): List[CrosswordXmlFile] = {
    val crosswordXmlFiles = s3Client
      .listObjects(crosswordsBucketName)
      .getObjectSummaries
      .asScala
      .toList
      .filter(_.getKey.endsWith(".xml"))
      .map(os => CrosswordXmlFile(os.getKey, getCrossword(crosswordsBucketName, os.getKey)))

    println(s"Found ${crosswordXmlFiles.size} crossword file(s) to process")

    crosswordXmlFiles
  }

  private def getCrossword(bucketName: String, key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(bucketName, key)
    download(obj)
  }

  private def download(obj: S3Object): Array[Byte] = {
    val out = new ByteArrayOutputStream(obj.getObjectMetadata.getContentLength.toInt)
    ByteStreams.copy(obj.getObjectContent, out)
    out.toByteArray
  }

  def archiveCrosswordXMLFile(config: Config, awsKey: String): Unit = {
    val archiveBucketName = "crossword-processed-files"
    println(s"Moving $awsKey to bucket $archiveBucketName")
    s3Client.copyObject(config.crosswordsBucketName, awsKey, archiveBucketName, awsKey)
    s3Client.deleteObject(config.crosswordsBucketName, awsKey)
  }

  def archiveFailedCrosswordXMLFile(config: Config, awsKey: String): Unit = {
    val processingFailedBucketName = "crossword-failed-files"
    s3Client.copyObject(config.crosswordsBucketName, awsKey, processingFailedBucketName, awsKey)
    if(s3Client.doesObjectExist(processingFailedBucketName, awsKey)) {
      s3Client.deleteObject(config.crosswordsBucketName, awsKey)
    }
  }
}
