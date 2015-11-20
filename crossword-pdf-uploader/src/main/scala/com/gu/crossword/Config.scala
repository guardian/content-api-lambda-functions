package com.gu.crossword

import java.util.Properties

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.AmazonS3Client

import scala.util.Try

class Config(val context: Context) {

  private val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(false)
  private val stage = if (isProd) "PROD" else "CODE"
  private val config = loadConfig()

  val crosswordMicroAppUrl = Option(config.getProperty("crosswordmicroapp.url")) getOrElse sys.error("'crosswordmicroapp.url' property missing.")
  val crosswordPdfPublicBucketName = s"crosswords-pdf-public-${stage.toLowerCase}"
  val crosswordPdfPublicFileLocation = s"https://s3-eu-west-1.amazonaws.com/$crosswordPdfPublicBucketName"

  private def loadConfig() = {
    val s3Client: AmazonS3Client = new AmazonS3Client()
    val configFileKey = s"crossword-pdf-uploader/$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-uploader-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}

