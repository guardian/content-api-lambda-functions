package com.gu.crossword

import java.io.ByteArrayOutputStream

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import com.google.common.io.ByteStreams
import com.gu.crossword.models.CrosswordXmlFile
import scala.collection.JavaConversions._

trait CrosswordStore {

  private val crosswordsBucketName: String = "crosswords-for-processing"
  private val s3Client: AmazonS3Client = new AmazonS3Client()

  def getCrosswordXmlFiles: List[CrosswordXmlFile] = {
    s3Client.listObjects(crosswordsBucketName).getObjectSummaries.toList
      .filter(_.getKey.endsWith(".xml"))
      .map(os => CrosswordXmlFile(os.getKey, getCrossword(os.getKey)))
  }

  private def getCrossword(key: String): Array[Byte] = {
    val obj: S3Object = s3Client.getObject(crosswordsBucketName, key)
    download(obj)
  }

  private def download(obj: S3Object): Array[Byte] = {
    val out = new ByteArrayOutputStream(obj.getObjectMetadata.getContentLength.toInt)
    ByteStreams.copy(obj.getObjectContent, out)
    out.toByteArray
  }

}
