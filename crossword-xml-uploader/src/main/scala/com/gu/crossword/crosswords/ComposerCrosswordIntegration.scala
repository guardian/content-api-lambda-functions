package com.gu.crossword.crosswords

import java.nio.ByteBuffer

import com.amazonaws.handlers.AsyncHandler
import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordXmlFile
import com.gu.crossword.services.Kinesis
import com.amazonaws.services.kinesis.model.{ PutRecordsRequestEntry, PutRecordsRequest, PutRecordsResult }

import scala.xml._

trait ComposerCrosswordIntegration extends Kinesis {

  def createPage(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem)(implicit config: Config): Unit = {

    val record = new PutRecordsRequestEntry()
      .withPartitionKey(crosswordXmlFile.key)
      .withData(ByteBuffer.wrap(crosswordXmlToCreatePage.toString.getBytes))

    val request = new PutRecordsRequest()
      .withStreamName(config.composerCrosswordIntegrationStreamName)
      .withRecords(record)

    kinesisClient.putRecordsAsync(request, new AsyncHandler[PutRecordsRequest, PutRecordsResult] {
      override def onError(exception: Exception): Unit =
        println(s"Crossword page creation request to Composer for crossword ${crosswordXmlFile.key} failed with error: $exception")

      override def onSuccess(request: PutRecordsRequest, result: PutRecordsResult): Unit =
        println(s"Crossword page creation request sent to Composer for crossword ${crosswordXmlFile.key}.")
    })

  }

}
