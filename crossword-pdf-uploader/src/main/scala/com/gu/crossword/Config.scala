package com.gu.crossword

import java.util.Properties

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.stores.S3Provider

import scala.util.Try

class Config(val context: Context) extends S3Provider {

  val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
  private val stage = if (isProd) "PROD" else "CODE"
  private val config = loadConfig()

  val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url")) getOrElse sys.error("'crosswordmicroapp.url' property missing.")
  val crosswordPdfPublicBucketName = s"crosswords-pdf-public-${stage.toLowerCase}"
  val crosswordPdfPublicFileLocation = if (isProd) s"https://crosswords-static.guim.co.uk" else s"https://s3-eu-west-1.amazonaws.com/$crosswordPdfPublicBucketName"

  private def loadConfig() = {
    val configFileKey = s"crossword-pdf-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}

