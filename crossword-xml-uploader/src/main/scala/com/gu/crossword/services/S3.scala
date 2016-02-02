package com.gu.crossword.services

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client

object S3 {
  // you might need to pass in 'new ProfileCredentialsProvider("composer)' here
  lazy val s3Client: AmazonS3Client = new AmazonS3Client()

}
