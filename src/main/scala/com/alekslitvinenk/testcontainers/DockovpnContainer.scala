package com.alekslitvinenk.testcontainers

import java.time.Duration

import org.testcontainers.containers.{FixedHostPortGenericContainer, InternetProtocol}
import org.testcontainers.containers.wait.strategy.Wait

object DockovpnContainer {
  val dockOvpnImageName = "alekslitvinenk/openvpn"
  
  object Tags {
    val latest = "latest"
  }
  
  def apply(tag: String = Tags.latest): DockovpnContainer = new DockovpnContainer(tag)
}

import DockovpnContainer._
class DockovpnContainer(tag: String)
  extends FixedHostPortGenericContainer[DockovpnContainer](s"$dockOvpnImageName:$tag") {
  
  private val httpPort = 8080
  private val udpPort = 1194
  private val logEntryRegEx = ".*Config\\sserver\\sstarted.*"
  private val startupTimeout = Duration.ofSeconds(10)
  
  addFixedExposedPort(httpPort, httpPort, InternetProtocol.TCP)
  addFixedExposedPort(udpPort, udpPort, InternetProtocol.UDP)
  withPrivilegedMode(true)
  waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
  
  def getHttpPort: Int = httpPort
  def getUdpPort: Int = udpPort
  def getConfigUrl = s"http://$getHost:$getHttpPort"
}
