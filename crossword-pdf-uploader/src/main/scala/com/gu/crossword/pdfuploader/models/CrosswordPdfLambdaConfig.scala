package com.gu.crossword.pdfuploader.models

case class CrosswordPdfLambdaConfig(
  crosswordMicroAppUrl: String,
  crosswordV2Url: Option[String],
  crosswordsBucketName: String,
  crosswordPdfPublicBucketName: String,
  crosswordPdfPublicFileLocation: String,
)