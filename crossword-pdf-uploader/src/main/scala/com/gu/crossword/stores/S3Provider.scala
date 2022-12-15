package com.gu.crossword.stores

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder

trait S3Provider {
  //  new ProfileCredentialsProvider("composer")
  protected val s3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
}
