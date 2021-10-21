package com.alekslitvinenk.testcontainers.dockovpn.loginterpreters

import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo
import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo.ClientInfoCallback

import java.time.Instant
import java.util.Date
import scala.util.matching.Regex

object ClientConnectInterpreter {
  def apply(callback: ClientInfoCallback) = new ClientConnectInterpreter(callback)
}

class ClientConnectInterpreter(callback: ClientInfoCallback) extends AbstractInterpreter(callback) {
  override protected val logEntriesRequired: Int = 2
  override val patternToDetectSequence: Regex = ".*Peer\\sConnection\\sInitiated\\swith.*".r
  
  override protected def parse(): ClientInfo = {
    val clientIdentifierRx = "[A-Za-z0-9]{32}"
    val ipAddressRx = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
    val localPortRx = "\\d{4,6}"
    val regExp: Regex =
      s"^.*($clientIdentifierRx)/($ipAddressRx):($localPortRx)\\s.*IPv4=($ipAddressRx).*".r
  
    val regExp(clientIdentifier, localIpAddress, localPort, vpnIpAddress) =
      logs.last.stripLineEnd
    
    ClientInfo(
      timeConnected = Date.from(Instant.now()),
      identifier = clientIdentifier,
      localIpAddress = localIpAddress,
      ipAddressWithinVPN = vpnIpAddress,
      vpnUdpPort = localPort.toInt,
    )
  }
}
