package com.gu.crossword.pdfuploader.models

case class CrosswordPdfLambdaConfig(
  crosswordMicroAppUrl: Option[String],
  crosswordV2Url: String,
  crosswordsBucketName: String,
  crosswordPdfPublicBucketName: String,
  crosswordPdfPublicFileLocation: String,
)