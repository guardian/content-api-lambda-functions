package com.gu.crossword.services

import com.amazonaws.services.s3.AmazonS3Client

object S3 {
  lazy val s3Client: AmazonS3Client = new AmazonS3Client()
}
