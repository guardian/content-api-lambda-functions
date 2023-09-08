package com.gu.crossword.pdfuploader

import com.amazonaws.services.s3.model.ObjectMetadata
import com.gu.crossword.pdfuploader.models.CrosswordPdfFile
import com.gu.crossword.services.AWS.s3Client

import java.io.ByteArrayInputStream
import scala.util.Try

trait PublicPdfStore {
  def uploadPdfCrosswordFile(bucketName: String, fileLocation: String, crosswordPdfFile: CrosswordPdfFile): Try[String]
}

trait S3PublicPdfStore extends PublicPdfStore {
  def uploadPdfCrosswordFile(bucketName: String, fileLocation: String, crosswordPdfFile: CrosswordPdfFile): Try[String] = Try {
    val is = new ByteArrayInputStream(crosswordPdfFile.file)
    val metadata = objectMetadata(crosswordPdfFile.file.size)

    s3Client.putObject(bucketName, crosswordPdfFile.awsKey, is, metadata)

    s"${fileLocation}/${crosswordPdfFile.awsKey}"
  }

  private def objectMetadata(length: Int): ObjectMetadata = {
    val md = new ObjectMetadata
    md.setContentType("application/pdf")
    md.setContentLength(length)
    md
  }
}
