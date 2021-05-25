package com.alekslitvinenk.testcontainers.log.interpreters

import com.alekslitvinenk.testcontainers.domain.ClientInfo
import com.alekslitvinenk.testcontainers.domain.ClientInfo.ClientInfoCallback

import scala.collection.mutable
import scala.util.matching.Regex

abstract class AbstractInterpreter(callback: ClientInfoCallback) {
  protected val logEntriesRequired: Int = 1
  protected val logs: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
  private var collectionStarted = false
  val patternToDetectSequence: Regex = "".r
  
  def startCollectingLogs(firstLogEntry: String): Unit = {
    collectionStarted = true
    appendLogEntry(firstLogEntry)
  }
  
  def isCollecting: Boolean = collectionStarted && logs.length < logEntriesRequired
  
  def appendLogEntry(logEntry: String): Unit = {
    logs += logEntry
  
    if (logs.length == logEntriesRequired) {
      collectionStarted = false
      callback(parse())
      
      logs.clear()
    }
  }
  
  protected def parse(): ClientInfo = ???
}
