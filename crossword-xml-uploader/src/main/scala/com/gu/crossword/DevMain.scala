package com.gu.crossword

import java.util
import scala.concurrent.duration.Duration

/**
 * Harness for running the Lambda on a dev machine.
 */
object DevMain extends App {

  val fakeScheduledEvent = new util.HashMap[String, Object]()

  val start = System.nanoTime()
  new Lambda().handleRequest(fakeScheduledEvent, null)
  val end = System.nanoTime()
  Console.err.println(s"Time taken: ${Duration.fromNanos(end - start).toMillis} ms")

}
