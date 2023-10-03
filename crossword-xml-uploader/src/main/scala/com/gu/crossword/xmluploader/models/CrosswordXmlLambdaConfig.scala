package com.gu.crossword.xmluploader.models

case class CrosswordXmlLambdaConfig(
                                  crosswordMicroAppUrl: Option[String],
                                  crosswordV2Url: String,
                                  composerCrosswordIntegrationStreamName: String,
                                  crosswordsBucketName: String
                                )
