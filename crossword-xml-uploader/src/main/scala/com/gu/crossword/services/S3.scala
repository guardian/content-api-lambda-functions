package com.gu.crossword.services

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.AmazonS3

object S3 {
  // you might need to pass in 'new ProfileCredentialsProvider("composer)' here
  lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()

}
