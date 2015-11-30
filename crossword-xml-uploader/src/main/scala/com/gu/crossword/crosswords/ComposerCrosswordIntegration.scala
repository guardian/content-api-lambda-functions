package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordXmlFile
import com.gu.crossword.services.Http.httpClient
import com.squareup.okhttp._
import scala.xml._

trait ComposerCrosswordIntegration extends XmlProcessor with CrosswordStore {

  def createPage(crosswordXmlFile: CrosswordXmlFile, crosswordXmlToCreatePage: Elem)(implicit config: Config): Unit = {
    val request = buildRequest(crosswordXmlToCreatePage)
    val response: Response = httpClient.newCall(request).execute()
    if (response.isSuccessful)
      println(s"Successfully created page for crossword ${crosswordXmlFile.key}. ${response.body.string}")
    else
      println(s"Creating page for crossword ${crosswordXmlFile.key} failed with error: ${response.body.string}")

  }

  private def buildRequest(crosswordXmlToCreatePage: Elem)(implicit config: Config) = {
    val requestBody: RequestBody = new MultipartBuilder()
      .`type`(MultipartBuilder.FORM)
      .addFormDataPart("fileData", "create-crossword-page.xml", RequestBody.create(MediaType.parse("application/xml"), crosswordXmlToCreatePage.toString))
      .build()

    new Request.Builder().url(config.composerCrosswordIntegrationUrl).post(requestBody).build()
  }

}
