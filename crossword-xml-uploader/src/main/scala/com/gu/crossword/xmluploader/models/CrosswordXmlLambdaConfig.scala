package com.gu.crossword.xmluploader.models

case class CrosswordXmlLambdaConfig(
                                  crosswordMicroAppUrl: String,
                                  crosswordV2Url: Option[String],
                                  composerCrosswordIntegrationStreamName: String,
                                  crosswordsBucketName: String
                                )
