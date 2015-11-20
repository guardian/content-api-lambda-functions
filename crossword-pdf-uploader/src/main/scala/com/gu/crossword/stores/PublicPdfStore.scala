package com.gu.crossword.stores

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.model.ObjectMetadata
import com.gu.crossword.Config
import com.gu.crossword.models.CrosswordPdfFile

trait PublicPdfStore extends S3Provider {

  def uploadPdfCrosswordFile(crosswordPdfFile: CrosswordPdfFile)(implicit config: Config): String = {
    val is = new ByteArrayInputStream(crosswordPdfFile.file)
    val metadata = objectMetadata(crosswordPdfFile.file.size)
    s3Client.putObject(config.crosswordPdfPublicBucketName, crosswordPdfFile.awsKey, is, metadata)

    s"${config.crosswordPdfPublicFileLocation}/${crosswordPdfFile.awsKey}"
  }

  private def objectMetadata(length: Int): ObjectMetadata = {
    val md = new ObjectMetadata
    md.setContentType("application/pdf")
    md.setContentLength(length)
    md
  }

}
