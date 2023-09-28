package com.gu.crossword.xmluploader

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.services.AWS.s3Client
import com.gu.crossword.xmluploader.models.CrosswordXmlLambdaConfig

import java.util.Properties
import scala.util.Try

trait CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordXmlLambdaConfig
}

trait S3CrosswordConfigRetriever extends CrosswordConfigRetriever {
  def getConfig(context: Context): CrosswordXmlLambdaConfig = {
    val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
    val stage = if (isProd) "PROD" else "CODE"
    val config = loadConfig(stage)

    val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url"))
    val crosswordV2Url = Option(config.getProperty("crosswordv2.url")) getOrElse sys.error("'crosswordv2.url' property missing.")

    val composerCrosswordIntegrationStreamName = Option(
      config.getProperty("composerCrosswordIntegration.streamName")).getOrElse(
      sys.error("'composerCrosswordIntegration.streamName' property missing")
    )

    val crosswordsBucketName: String =
      if (isProd)
        "crossword-files-for-processing"
      else
        "crossword-files-for-processing-code"

    CrosswordXmlLambdaConfig(
      crosswordMicroAppUrl,
      crosswordV2Url,
      composerCrosswordIntegrationStreamName,
      crosswordsBucketName
    )
  }

  private def loadConfig(stage: String) = {
    println("Loading config for stage: " + stage)

    val configFileKey = s"crossword-xml-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()

    try {
      configFile.load(configInputStream)
    } catch {
      case e: Exception =>
        println(s"Failed to load config file from s3. This lambda will not run. Error: ${e.getMessage}")
        sys.exit(1)
    }

    configFile
  }
}
