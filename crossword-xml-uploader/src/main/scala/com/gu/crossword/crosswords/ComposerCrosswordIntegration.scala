package com.gu.crossword.crosswords

import java.nio.ByteBuffer

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordXmlFile
import com.gu.crossword.services.Kinesis
import com.amazonaws.services.kinesis.model.{ PutRecordsRequestEntry, PutRecordsRequest, PutRecordsResult }

import scala.xml._

trait ComposerCrosswordIntegration extends Kinesis with CrosswordStore {

  def createPage(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem)(implicit config: Config): Unit = {

    val record = new PutRecordsRequestEntry()
      .withPartitionKey(crosswordXmlFile.key)
      .withData(ByteBuffer.wrap(crosswordXmlToCreatePage.toString.getBytes))

    val request = new PutRecordsRequest()
      .withStreamName(config.composerCrosswordIntegrationStreamName)
      .withRecords(record)

    val putRecordsResult: PutRecordsResult = kinesisClient.putRecords(request)
    if (putRecordsResult.getFailedRecordCount > 0) {
      println(s"Crossword page creation request to Composer for crossword ${crosswordXmlFile.key} failed.")
    } else {
      println(s"Crossword page creation request sent to Composer for crossword ${crosswordXmlFile.key}.")
      if (config.isProd) archiveCrosswordXMLFile(config, crosswordXmlFile.key)
    }
  }

}
