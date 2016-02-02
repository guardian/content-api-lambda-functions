package com.gu.crossword

import java.util.{ Map => JMap }
import com.amazonaws.services.lambda.runtime.{ RequestHandler, Context }
import com.gu.crossword.models.CrosswordPdfFile
import com.gu.crossword.stores.{ PublicPdfStore, CrosswordStore }
import com.squareup.okhttp._
import org.apache.http.HttpStatus

class Lambda
    extends RequestHandler[JMap[String, Object], Unit]
    with CrosswordUploader
    with CrosswordStore
    with PublicPdfStore {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {
    implicit val config = new Config(context)

    println("The uploading of crossword pdf files has started.")

    for {
      crosswordPdfFile <- getCrosswordPdfFiles
    } yield {
      val location = uploadPdfCrosswordFile(crosswordPdfFile)
      handleResponse(uploadPdfCrosswordLocation(crosswordPdfFile, location), crosswordPdfFile)
    }

    println("The uploading of crossword pdf files has finished.")
  }

  private def handleResponse(response: Response, crosswordPdfFile: CrosswordPdfFile) = {
    println(s"Microapp response: '${response.message}' with status code: ${response.code}")
    if (response.isSuccessful) {
      archiveProcessedPdfFiles(crosswordPdfFile.awsKey)
      println(s"Successfully uploaded crossword ${crosswordPdfFile.awsKey}")
    } else if (response.code() == HttpStatus.SC_NOT_FOUND) {
      println(s"Looks like the crossword microapp could not find the relevant crossword for ${crosswordPdfFile.awsKey}. " +
        s"Are you sure its counterpart xml file has been uploaded first?")
    } else println(s"Upload of crossword PDF location for ${crosswordPdfFile.awsKey} failed.")
  }

}