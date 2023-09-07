package com.gu.crossword.crosswords

import scala.xml._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try

trait DateLogic {

  def transformDate(dateString: String): String = {
    val inputFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").withZone(DateTimeZone.forID("Europe/London"))
    val outputFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").withZone(DateTimeZone.forID("UTC"))
    DateTime.parse(dateString, inputFormat).toString(outputFormat)
  }

}

object XmlProcessor extends DateLogic {

  def process(crosswordXml: Elem): Either[Throwable, Elem] = Try {

    implicit val xmlToProcess: Elem = crosswordXml

    val cmsPath = getElementText("cms-path")
    val headline = getElementText("headline")
    val linktext = getElementText("link-text")
    val trailtext = getElementText("trail-text")
    val publication = getElementText("publication")
    val commentable = getElementText("commentable")
    val externalReferences = getExternalReferences
    val webPublicationDate = getDate("web-publication-date")
    val issueDate = getDate("issue-date")

    <crossword notes="Notes for crossword article" cms-path={ cmsPath } issue-date={ issueDate } web-publication-date={ webPublicationDate } enable-comments={ commentable }>
      <headline>{ headline }</headline>
      <linktext>{ linktext }</linktext>
      <trail>{ trailtext }</trail>
      <publication>{ publication }</publication>
      <externalReferences>
        {
          for {
            exRef <- externalReferences
          } yield <externalReference type={ exRef._1 } token={ exRef._2 }/>
        }
      </externalReferences>
    </crossword>

  }.toEither

  private def getElementText(elementName: String)(implicit crosswordXml: Elem) = {
    (crosswordXml \\ elementName).text
  }

  private def getDate(elementName: String)(implicit crosswordXml: Elem): String = {
    val dateString = (crosswordXml \\ elementName).text
    transformDate(dateString)
  }

  private def getExternalReferences(implicit crosswordXml: Elem): Seq[(String, String)] = {
    val references = (crosswordXml \\ "reference").toSeq

    for {
      reference <- references
    } yield ((reference \\ "type").text, (reference \\ "token").text)
  }

}
