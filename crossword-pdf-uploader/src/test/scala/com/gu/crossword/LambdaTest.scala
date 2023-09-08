package com.gu.crossword

import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.pdfuploader.models.{CrosswordPdfFile, CrosswordPdfLambdaConfig}
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source
import scala.util.{Success, Try}
import scala.xml.{Elem, XML}


class LambdaTest extends AnyFlatSpec with Matchers with TryValues {

  type PageCreator = (String, Elem) => Try[Unit]
  type UploadCrosswordFile = (String, String, CrosswordPdfFile) => Try[String]
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
                       uploadCrosswordFile: UploadCrosswordFile = (_, _, _) => Success(""),
                       uploadCrosswordLocation: UploadCrosswordLocation = (_, _, _) => Success(Unit),
                     ) = {
    new FakeLambda {
      override def getCrosswordPdfFiles(): List[CrosswordPdfFile] = crosswordPdfFiles

      override def getConfig(context: Context): CrosswordPdfLambdaConfig = CrosswordPdfLambdaConfig(
        crosswordPdfPublicBucketName = "crossword-pdf-public-bucket-name",
        crosswordPdfPublicFileLocation = "crossword-pdf-public-file-location",
        crosswordMicroAppUrl = "https://crossword-microapp-url",
        crosswordsBucketName = "crosswords-bucket-name",
      )

      override def uploadPdfCrosswordLocation(url: String, crosswordPdfFile: CrosswordPdfFile, location: String): Try[Unit] =
        uploadCrosswordLocation(url, location, crosswordPdfFile)

      override def uploadPdfCrosswordFile(bucketName: String, fileLocation: String, crosswordPdfFile: CrosswordPdfFile): Try[String] =
        uploadCrosswordFile(bucketName, fileLocation, crosswordPdfFile)
    }
  }

  val crosswordMicroAppResponse = Source.fromResource("example-crossword-microapp-response-quiptic-834.xml").getLines().mkString
  val crosswordMicroAppResponseXml = XML.loadString(crosswordMicroAppResponse)

  it should "archive correctly a successfully processed crossword pdf" in {
    true shouldBe false
  }
}