package com.gu.crossword.pdfuploader.models

case class CrosswordPdfLambdaConfig(
  crosswordMicroAppUrl: String,
  crosswordsBucketName: String,
  crosswordPdfPublicBucketName: String,
  crosswordPdfPublicFileLocation: String,
)