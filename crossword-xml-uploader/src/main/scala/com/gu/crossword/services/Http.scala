package com.gu.crossword.services

import com.squareup.okhttp.OkHttpClient

object Http {
  lazy val httpClient: OkHttpClient = new OkHttpClient()
}
