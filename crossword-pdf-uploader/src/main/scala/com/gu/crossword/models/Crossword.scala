package com.gu.crossword.models

import org.joda.time.{ DateTimeConstants, DateTime, LocalDate }
import scala.util.{ Success, Try }

case class CrosswordPdfFile(awsKey: String, filename: CrosswordPdfFileName, file: Array[Byte])

case class CrosswordPdfFileName(year: String, month: String, day: String, `type`: String) {
  def getPublicationDate: DateTime = new LocalDate(year.toInt, month.toInt, day.toInt).toDateTimeAtStartOfDay
}

object CrosswordPdfFileName {

  /* Key expected in format: blah.crossword_type.YYYYMMDD.blah.blah.pdf */
  def apply(key: String): Option[CrosswordPdfFileName] = {

    val attemptKeyParse = Try {
      val nameParts: List[String] = key.split("\\.").toList
      val year = nameParts(2).substring(0, 4)
      val month = nameParts(2).substring(4, 6)
      val day = nameParts(2).substring(6, 8)

      val publicationDate: DateTime = new LocalDate(year.toInt, month.toInt, day.toInt).toDateTimeAtStartOfDay

      val `type`: String = nameParts(1) match {
        case "cryptic" if publicationDate.getDayOfWeek == DateTimeConstants.SATURDAY => "prize"
        case crosswordType => crosswordType
      }

      CrosswordPdfFileName(year, month, day, `type`)
    }

    attemptKeyParse match {
      case Success(crosswordPdfFileName) => Some(crosswordPdfFileName)
      case _ => {
        println(s"Could not parse key: $key - this file will be ignored.")
        None
      }
    }

  }
}