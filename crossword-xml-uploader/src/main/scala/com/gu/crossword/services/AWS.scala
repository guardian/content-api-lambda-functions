package com.gu.crossword.services

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.AmazonKinesisAsync
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

import scala.util.Try

object AWS {
  // This environment variable is set by AWS Lambda
  lazy val inAws = Try(sys.env("AWS_EXECUTION_ENV")).isSuccess
  lazy val composerCredentials = new ProfileCredentialsProvider("composer")

  lazy val kinesisAsyncClientBuilder = AmazonKinesisAsyncClientBuilder
    .standard()
    .withRegion(Regions.EU_WEST_1)

  lazy val s3ClientBuilder = AmazonS3ClientBuilder
    .standard()
    .withRegion(Regions.EU_WEST_1)

  lazy val kinesisClient: AmazonKinesisAsync = if(inAws){
    kinesisAsyncClientBuilder.build()
  } else {
    kinesisAsyncClientBuilder.withCredentials(composerCredentials).build()
  }

  lazy val s3Client: AmazonS3 = if(inAws){
    s3ClientBuilder.build()
  } else {
    s3ClientBuilder.withCredentials(composerCredentials).build()
  }
}
