package com.gu.crossword.crosswords

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.crosswords.models.CrosswordLambdaConfig
import com.gu.crossword.services.AWS.s3Client

import java.util.Properties
import scala.util.Try

trait CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordLambdaConfig
}

trait S3CrosswordConfigRetriever extends CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordLambdaConfig = {
    val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
    val stage = if (isProd) "PROD" else "CODE"
    val config = loadConfig(stage)

    val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url")) getOrElse sys.error("'crosswordmicroapp.url' property missing.")
    val composerCrosswordIntegrationStreamName = Option(
      config.getProperty("composerCrosswordIntegration.streamName")).getOrElse(
      sys.error("'composerCrosswordIntegration.streamName' property missing")
    )

    val crosswordsBucketName: String =
      if (isProd)
        "crossword-files-for-processing"
      else
        "crossword-files-for-processing-code"

    CrosswordLambdaConfig(
      crosswordMicroAppUrl,
      composerCrosswordIntegrationStreamName,
      crosswordsBucketName
    )
  }

  private def loadConfig(stage: String) = {
    println("Loading config for stage: " + stage)

    val configFileKey = s"crossword-xml-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}
