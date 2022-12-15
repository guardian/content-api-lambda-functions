package com.gu.crossword.services

import okhttp3.OkHttpClient

object Http {
  lazy val httpClient: OkHttpClient = new OkHttpClient()
}
