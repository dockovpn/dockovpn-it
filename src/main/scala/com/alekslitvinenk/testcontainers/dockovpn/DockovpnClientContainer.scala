package com.alekslitvinenk.testcontainers.dockovpn

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnClientContainer.dockOvpnClientImageName
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.time.Duration

/**
 * Scala wrapper for [[https://github.com/dockovpn/docker-openvpn-client Dockovpn-client]]: simple OpenVpn client
 * @param tag
 */
class DockovpnClientContainer(tag: String)
  extends GenericContainer[DockovpnClientContainer](s"$dockOvpnClientImageName:$tag") {
  
  private val logEntryRegEx = ".*Peer\\sConnection\\sInitiated\\swith.*"
  private val startupTimeout = Duration.ofSeconds(10)
  
  withPrivilegedMode(true)
  waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
  
  /**
   * Returns directory where Dockovpn client expects to find client.ovpn file
   * @return
   */
  def getConfigDir: String = "/opt/Dockovpn_data"
}

object DockovpnClientContainer {
  val dockOvpnClientImageName = "alekslitvinenk/openvpn-client"
  
  def apply(tag: String = Tags.latest): DockovpnClientContainer = new DockovpnClientContainer(Tags.latest)
  
  object Tags {
    val latest = "latest"
  }
}
