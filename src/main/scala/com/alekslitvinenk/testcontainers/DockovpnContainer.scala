package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.domain.ClientInfo
import com.alekslitvinenk.testcontainers.domain.ClientInfo.ClientInfoCallback
import com.alekslitvinenk.testcontainers.log.interpreters.{AbstractInterpreter, ClientConnectInterpreter}
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.{FixedHostPortGenericContainer, InternetProtocol}

import java.net.URL
import java.time.Duration
import scala.collection.mutable
import scala.io.Source
import scala.util.Try

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

import com.alekslitvinenk.testcontainers.DockovpnContainer._
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
  
  def getHttpPort: Int = httpPort
  def getUdpPort: Int = udpPort
  def getConfigUrl = s"http://$getHost:$getHttpPort"
  
  def downloadClientConfig(hostOpt: Option[String] = None): Try[String] = Try {
    val configUrl = hostOpt.map(host => s"http://$host:$getHttpPort").getOrElse(this.getConfigUrl)
    
    Source.fromURL(new URL(configUrl))
  } map {_.getLines().reduce { (a: String, b: String) =>
    s"$a\n$b"
  }}
  
  def onClientConnected(callback: ClientInfoCallback): Unit = {
    def  wrappedCallback(clientInfo: ClientInfo): Unit = {
      activeClients += clientInfo
      callback(clientInfo)
    }
    
    logInterpreters += ClientConnectInterpreter(wrappedCallback)
  }
  
  def onClientDisconnected(callback: ClientInfo => Unit): Unit = {
  
  }
  
  def getActiveClients: List[ClientInfo] = activeClients.toList
  
  def executeVersionCommand(): Unit = {
  
  }
  
  def executeGenclientVersion(): Unit = {
  
  }
  
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
