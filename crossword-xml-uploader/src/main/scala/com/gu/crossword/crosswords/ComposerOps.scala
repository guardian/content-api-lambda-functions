package com.gu.crossword.crosswords

import java.nio.ByteBuffer
import com.gu.crossword.crosswords.models._
import com.gu.crossword.services.AWS.kinesisClient
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry}
import scala.xml._


trait ComposerOps {
  def createPage(composerCrosswordIntegrationStreamName: String)(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem): Either[Error, Unit]
}

trait KinesisComposerOps extends ComposerOps {
  def createPage(composerCrosswordIntegrationStreamName: String)(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem): Either[Error, Unit] = {
    val record = new PutRecordsRequestEntry()
      .withPartitionKey(crosswordXmlFile.key)
      .withData(ByteBuffer.wrap(crosswordXmlToCreatePage.toString.getBytes))

    val request = new PutRecordsRequest()
      .withStreamName(composerCrosswordIntegrationStreamName)
      .withRecords(record)

    if (kinesisClient.putRecords(request).getFailedRecordCount > 0) {
      Left(new Error(s"Crossword page creation request to Composer for crossword ${crosswordXmlFile.key} failed."))
    } else {
      Right(())
    }
  }
}