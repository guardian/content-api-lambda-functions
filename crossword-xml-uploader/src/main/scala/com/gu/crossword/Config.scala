package com.gu.crossword

import java.util.Properties
import com.amazonaws.regions.{ Regions, Region }
import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.services.S3.s3Client
import scala.util.Try

class Config(val context: Context) {

  val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
  private val stage = if (isProd) "PROD" else "CODE"
  private val config = loadConfig()

  val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url")) getOrElse sys.error("'crosswordmicroapp.url' property missing.")
  val composerCrosswordIntegrationStreamName = Option(config.getProperty("composerCrosswordIntegration.streamName")) getOrElse sys.error("'composerCrosswordIntegration.streamName' property missing")

  val crosswordsBucketName: String =
    if (isProd)
      "crossword-files-for-processing"
    else
      "crossword-files-for-processing-code"

  private def loadConfig() = {
    val configFileKey = s"crossword-xml-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}
