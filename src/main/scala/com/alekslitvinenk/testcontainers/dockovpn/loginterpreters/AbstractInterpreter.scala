package com.alekslitvinenk.testcontainers.dockovpn.loginterpreters

import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo
import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo.ClientInfoCallback

import scala.collection.mutable
import scala.util.matching.Regex

abstract class AbstractInterpreter(callback: ClientInfoCallback) {
  
  protected val logs: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
  private var collectionStarted = false
  
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
  
  def patternToDetectSequence: Regex = ???
  
  protected def logEntriesRequired: Int = ???
  
  protected def parse(): ClientInfo = ???
}
