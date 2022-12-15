package com.gu.crossword.services

import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.AmazonKinesisAsync
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder

trait Kinesis {

  lazy val kinesisClient: AmazonKinesisAsync = AmazonKinesisAsyncClientBuilder
    .standard()
    .withRegion(Regions.EU_WEST_1)
    .build()

}
