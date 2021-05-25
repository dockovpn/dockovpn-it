package com.alekslitvinenk.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.time.Duration

object DockovpnClientContainer {
  
  val dockOvpnClientImageName = "alekslitvinenk/openvpn-client"
  
  def apply(tag: String = Tags.latest): DockovpnClientContainer = new DockovpnClientContainer(Tags.latest)
  
  object Tags {
    val latest = "latest"
  }
}

import com.alekslitvinenk.testcontainers.DockovpnClientContainer._

class DockovpnClientContainer(tag: String) extends GenericContainer[DockovpnClientContainer](s"$dockOvpnClientImageName:$tag") {
  private val logEntryRegEx = ".*Peer\\sConnection\\sInitiated\\swith.*"
  private val startupTimeout = Duration.ofSeconds(10)
  
  withPrivilegedMode(true)
  waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
  
  def getConfigDir: String = "/opt/Dockovpn_data"
}
