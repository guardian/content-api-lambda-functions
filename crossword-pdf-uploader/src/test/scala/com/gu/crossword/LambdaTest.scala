package com.gu.crossword

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.pdfuploader.models.{CrosswordPdfFile, CrosswordPdfFileName, CrosswordPdfLambdaConfig}
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}
import scala.xml.Elem


class LambdaTest extends AnyFlatSpec with Matchers with TryValues {

  type PageCreator = (String, Elem) => Try[Unit]
  type UploadCrosswordFile = (String, String, CrosswordPdfFile) => Try[Unit]
  type UploadCrosswordLocation = (String, String, CrosswordPdfFile) => Try[Unit]

  trait FakeLambda extends CrosswordPdfUploaderLambda {
    var archiveCalled = 0
    var archiveFailedCalled = 0

    def archiveProcessedPdfFiles(bucketName: String, key: String): Unit = {
      archiveCalled += 1
    }

    def archiveFailedPdfFiles(bucketName: String, key: String): Unit = {
      archiveFailedCalled += 1
    }
  }

  def buildFakeLambda(
                       crosswordPdfFiles: List[CrosswordPdfFile] = List.empty,
                       uploadCrosswordFile: UploadCrosswordFile = (_, _, _) => Success(()),
                       uploadCrosswordLocation: UploadCrosswordLocation = (_, _, _) => Success(()),
                     ): FakeLambda = {
    new FakeLambda {
      override def getCrosswordPdfFiles(bucketName: String): List[CrosswordPdfFile] = crosswordPdfFiles

      override def getConfig(context: Context): CrosswordPdfLambdaConfig = CrosswordPdfLambdaConfig(
        crosswordPdfPublicBucketName = "crossword-pdf-public-bucket-name",
        crosswordPdfPublicFileLocation = "crossword-pdf-public-file-location",
        crosswordMicroAppUrl = "https://crossword-microapp-url",
        crosswordsBucketName = "crosswords-bucket-name",
      )

      override def uploadPdfCrosswordLocation(url: String, crosswordPdfFile: CrosswordPdfFile, location: String): Try[Unit] =
        uploadCrosswordLocation(url, location, crosswordPdfFile)

      override def uploadPdfCrosswordFile(bucketName: String, fileLocation: String, crosswordPdfFile: CrosswordPdfFile): Try[Unit] =
        uploadCrosswordFile(bucketName, fileLocation, crosswordPdfFile)
    }
  }

  it should "archive correctly a successfully processed crossword pdf" in {
    var uploadCrosswordLocationCalled = List.empty[(String, String, CrosswordPdfFile)]
    def uploadCrosswordLocation(url: String, location: String, crosswordPdfFile: CrosswordPdfFile): Try[Unit] = {
      uploadCrosswordLocationCalled = uploadCrosswordLocationCalled :+ (url, location, crosswordPdfFile)
      Success(())
    }

    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val fakeLambda = buildFakeLambda(
      crosswordPdfFiles = List(CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty)),
      uploadCrosswordLocation = uploadCrosswordLocation
    )

    fakeLambda.handleRequest(null, null)

    // Check location is constructed as expected
    uploadCrosswordLocationCalled.size should be(1)
    uploadCrosswordLocationCalled.head match {
      case (_, location, _) =>
        location should be("crossword-pdf-public-file-location/gdn.cryptic.20230418.pdf")
    }

    fakeLambda.archiveCalled should be(1)
    fakeLambda.archiveFailedCalled should be(0)
  }

  it should "archive as failure a processed crossword if uploading to crossword service fails" in {
    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val fakeLambda = buildFakeLambda(
      crosswordPdfFiles = List(CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty)),
      uploadCrosswordFile = (_, _, _) => Failure(new Exception("BOOM!"))
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include(
      s"Failures detected when uploading crossword PDF files ($fileName)!"
    )

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }

  it should "archive as failure a processed crossword if uploading toa public bucket fails" in {
    val fileName = "gdn.cryptic.20230418.pdf"
    val crosswordPdfFile = CrosswordPdfFileName(fileName).get

    val fakeLambda = buildFakeLambda(
      crosswordPdfFiles = List(CrosswordPdfFile(fileName, crosswordPdfFile, Array.empty)),
      uploadCrosswordLocation = (_, _, _) => Failure(new Exception("BOOM!"))
    )

    val result = Try(fakeLambda.handleRequest(null, null)).failed.get
    result.getMessage should include(
      s"Failures detected when uploading crossword PDF files ($fileName)!"
    )

    fakeLambda.archiveCalled should be(0)
    fakeLambda.archiveFailedCalled should be(1)
  }
}