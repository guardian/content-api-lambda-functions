package com.gu.crossword.crosswords.models

case class CrosswordLambdaConfig(
                                  crosswordMicroAppUrl: String,
                                  composerCrosswordIntegrationStreamName: String,
                                  crosswordsBucketName: String
                                )
