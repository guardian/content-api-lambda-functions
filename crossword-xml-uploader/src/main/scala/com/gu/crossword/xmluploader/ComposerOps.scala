package com.gu.crossword.xmluploader

import java.nio.ByteBuffer
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry}
import com.gu.crossword.services.AWS.kinesisClient

import scala.util.Try
import scala.xml._


trait ComposerOps {
  def createPage(streamName: String)(key: String, xmlData: Elem): Try[Unit]
}

trait KinesisComposerOps extends ComposerOps {
  def createPage(streamName: String)(key: String, xmlData: Elem): Try[Unit] = Try {
    val record = new PutRecordsRequestEntry()
      .withPartitionKey(key)
      .withData(ByteBuffer.wrap(xmlData.toString.getBytes))

    val request = new PutRecordsRequest()
      .withStreamName(streamName)
      .withRecords(record)

    if (kinesisClient.putRecords(request).getFailedRecordCount > 0) {
      throw new Exception(
        s"Crossword page creation request to Composer for crossword ${key} failed."
      )
    }
  }
}