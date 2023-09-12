package com.gu.crossword.crosswords.models

case class CrosswordLambdaConfig(
                                  crosswordMicroAppUrl: String,
                                  crosswordV2Url: Option[String],
                                  composerCrosswordIntegrationStreamName: String,
                                  crosswordsBucketName: String
                                )
