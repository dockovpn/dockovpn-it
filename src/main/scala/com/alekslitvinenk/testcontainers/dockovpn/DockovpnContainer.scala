package com.alekslitvinenk.testcontainers.dockovpn

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnContainer.dockOvpnImageName
import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo
import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo.ClientInfoCallback
import com.alekslitvinenk.testcontainers.dockovpn.loginterpreters.{AbstractInterpreter, ClientConnectInterpreter}
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.{FixedHostPortGenericContainer, InternetProtocol}

import java.net.URL
import java.time.Duration
import scala.collection.mutable
import scala.io.Source
import scala.util.Try
import com.alekslitvinenk.testcontainers.dockovpn.commands.DockovpnCommands

/**
 * Scala wrapper for [[https://dockovpn.io Dockovpn]]
 * @param tag tag to use
 */
class DockovpnContainer(tag: String)
  extends FixedHostPortGenericContainer[DockovpnContainer](s"$dockOvpnImageName:$tag") {
  
  private val httpPort = 8080
  private val udpPort = 1194
  // Fri May 14 19:45:05 2021 Config server started, download your client.ovpn config at http://x.x.x.x
  private val logEntryRegEx = ".*Config\\sserver\\sstarted.*"
  private val startupTimeout = Duration.ofSeconds(10)
  private val activeClients = mutable.ArrayBuffer[ClientInfo]()
  private val logInterpreters = mutable.ArrayBuffer[AbstractInterpreter]()
  
  addFixedExposedPort(httpPort, httpPort, InternetProtocol.TCP)
  addFixedExposedPort(udpPort, udpPort, InternetProtocol.UDP)
  withPrivilegedMode(true)
  withLogConsumer(logConsumer)
  waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
  
  /**
   * Returns TCP port to which embedded HTTP server binds to serve client config
   * @return
   */
  def getHttpPort: Int = httpPort
  
  /**
   * Returns UDP port to which OpenVpn server within container binds
   * @return
   */
  def getUdpPort: Int = udpPort
  
  /**
   * Returns url for fetching client configuration
   * @return
   */
  def getConfigUrl = s"http://$getHost:$getHttpPort"
  
  /**
   * Dockovpn specific commands
   */
  lazy val commands: DockovpnCommands = DockovpnCommands(this)
  
  /**
   * Downloads client configuration at host hostOpt.
   * if no value provided for the hostOpt then value specified in HOST_ADDR
   * environment variable used
   * @param hostOpt Option of host string
   * @return
   */
  def downloadClientConfig(hostOpt: Option[String] = None): Try[String] = Try {
    val configUrl = hostOpt.map(host => s"http://$host:$getHttpPort").getOrElse(this.getConfigUrl)
    
    Source.fromURL(new URL(configUrl))
  } map {_.getLines().reduce { (a: String, b: String) =>
    s"$a\n$b"
  }}
  
  /**
   * Registers a callback which gets invoked when a new client connects to Dockovpn
   * @param callback function that takes [[ClientInfo]] object as a parameter and returns nothing
   */
  def onClientConnected(callback: ClientInfoCallback): Unit = {
    def wrappedCallback(clientInfo: ClientInfo): Unit = {
      activeClients += clientInfo
      callback(clientInfo)
    }
  
    logInterpreters += ClientConnectInterpreter(wrappedCallback)
  }
  
  /**
   * Registers a callback which gets invoked when an active client disconnects from Dockovpn
   * @param callback function that takes [[ClientInfo]] object as a parameter and returns nothing
   */
  def onClientDisconnected(callback: ClientInfo => Unit): Unit = ???
  
  /**
   * Returns immutable list of active clients at the moment of calling getActiveClients method
   * @return list of active clients
   */
  def getActiveClients: List[ClientInfo] = activeClients.toList
  
  private def logConsumer(outputFrame: OutputFrame) {
    val logEntry = outputFrame.getUtf8String.stripLineEnd
    
    logInterpreters.foreach { interpreter =>
      if (interpreter.isCollecting) {
        interpreter.appendLogEntry(logEntry)
      } else {
        if (interpreter.patternToDetectSequence.pattern.matcher(logEntry).matches()) {
          interpreter.startCollectingLogs(logEntry)
        }
      }
    }
  }
}

object DockovpnContainer {
  val dockOvpnImageName = "alekslitvinenk/openvpn"
  
  object Tags {
    val latest = "latest"
  }
  
  object Envs {
    val hostAddress = "HOST_ADDR"
  }
  
  def apply(tag: String = Tags.latest): DockovpnContainer = new DockovpnContainer(tag)
}
