package com.gu.crossword.xmluploader

import com.gu.crossword.xmluploader.models._
import scala.xml.{Elem, XML}

import scala.util.Try

trait CrosswordXmlUploader extends CrosswordClientOps {
  def uploadCrossword(crosswordMicroAppUrl: String)(crosswordXmlFile: CrosswordXmlFile): Try[Elem] = {
    for {
      responseBody <- upload(crosswordMicroAppUrl)(crosswordXmlFile.key, crosswordXmlFile.file)
      rawXml <- Try(XML.loadString(responseBody))
    } yield rawXml
  }
}