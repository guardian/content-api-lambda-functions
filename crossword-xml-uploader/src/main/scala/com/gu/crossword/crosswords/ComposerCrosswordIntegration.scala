package com.gu.crossword.crosswords

import java.nio.ByteBuffer
import com.gu.crossword.Config
import com.gu.crossword.crosswords.models._
import com.gu.crossword.services.Kinesis
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry}
import scala.xml._

object Composer extends Kinesis {

  def createPage(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem)(implicit config: Config): Either[Error, Unit] = {
    val record = new PutRecordsRequestEntry()
      .withPartitionKey(crosswordXmlFile.key)
      .withData(ByteBuffer.wrap(crosswordXmlToCreatePage.toString.getBytes))

    val request = new PutRecordsRequest()
      .withStreamName(config.composerCrosswordIntegrationStreamName)
      .withRecords(record)

    if (kinesisClient.putRecords(request).getFailedRecordCount > 0) {
      Left(new Error(s"Crossword page creation request to Composer for crossword ${crosswordXmlFile.key} failed."))
    } else {
      Right()
    }
  }
}