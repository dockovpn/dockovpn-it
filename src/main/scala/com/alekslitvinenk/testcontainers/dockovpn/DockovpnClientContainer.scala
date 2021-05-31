package com.alekslitvinenk.testcontainers.dockovpn

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnClientContainer.dockOvpnClientImageName
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.time.Duration

/**
 * Scala wrapper for [[https://github.com/dockovpn/docker-openvpn-client Dockovpn-client]]: simple OpenVpn client
 * @param tag
 */
class DockovpnClientContainer(configName: String, tag: String)
  extends GenericContainer[DockovpnClientContainer](s"$dockOvpnClientImageName:$tag") {
  
  private val logEntryRegEx = ".*Peer\\sConnection\\sInitiated\\swith.*"
  private val startupTimeout = Duration.ofSeconds(10)
  
  withCommand(configName)
  withPrivilegedMode(true)
  waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
}

object DockovpnClientContainer {
  val dockOvpnClientImageName = "alekslitvinenk/openvpn-client"
  /**
   * Returns directory where Dockovpn client expects to find config files
   * @return
   */
  val getConfigDir: String = "/opt/Dockovpn_data"
  
  def apply(configName: String, tag: String = Tags.latest): DockovpnClientContainer =
    new DockovpnClientContainer(configName: String, Tags.latest)
  
  object Tags {
    val latest = "latest"
  }
}
