package com.gu.crossword.crosswords.models

case class CrosswordLambdaConfig(
                                  crosswordMicroAppUrl: String,
                                  crosswordV2Url: String,
                                  composerCrosswordIntegrationStreamName: String,
                                  crosswordsBucketName: String
                                )
