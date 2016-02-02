package com.gu.crossword.services

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClient
import com.gu.crossword.Config

trait Kinesis {

  lazy val kinesisClient: AmazonKinesisAsyncClient = {
    val kinesisClient = new AmazonKinesisAsyncClient(new ProfileCredentialsProvider("composer"))
    kinesisClient.setRegion(Region.getRegion(Regions.EU_WEST_1))
    kinesisClient

  }
}
