package com.gu.crossword.crosswords

import com.gu.crossword.crosswords.models._
import scala.xml.{Elem, XML}

import scala.util.Try

trait CrosswordUploader extends CrosswordClientOps {
  def uploadCrossword(crosswordMicroAppUrl: String)(crosswordXmlFile: CrosswordXmlFile): Either[Throwable, Elem] = {
    for {
      responseBody <- upload(crosswordMicroAppUrl)(crosswordXmlFile.key, crosswordXmlFile.file)
      rawXml <- Try {
        XML.loadString(responseBody)
      }.toEither
    } yield rawXml
  }
}