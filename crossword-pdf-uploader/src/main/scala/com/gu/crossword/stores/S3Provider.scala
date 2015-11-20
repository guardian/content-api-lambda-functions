package com.gu.crossword.stores

import com.amazonaws.services.s3.AmazonS3Client

trait S3Provider {
  protected val s3Client: AmazonS3Client = new AmazonS3Client()
}
