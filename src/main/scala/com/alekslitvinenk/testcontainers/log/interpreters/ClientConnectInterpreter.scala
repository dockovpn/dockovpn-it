package com.alekslitvinenk.testcontainers.log.interpreters

import com.alekslitvinenk.testcontainers.domain.ClientInfo
import com.alekslitvinenk.testcontainers.domain.ClientInfo.ClientInfoCallback
import com.github.nscala_time.time.Imports.DateTime

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
    val dateRx = "[A-Za-z]{3}\\s[A-Za-z]{3}\\s\\d{1,2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{4}"
    val clientIdentifierRx = "[A-Za-z0-9]+"
    val ipAddressRx = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"
    val localPortRx = "\\d{4,6}"
    val regExp: Regex =
      s"^($dateRx)\\s($clientIdentifierRx)/($ipAddressRx):($localPortRx)\\s.*IPv4=($ipAddressRx).*".r
  
    val regExp(date, clientIdentifier, localIpAddress, localPort, vpnIpAddress) =
      logs.last.stripLineEnd
    
    ClientInfo(
      // FixMe: use date from parsed date string
      timeConnected = Date.from(Instant.now()),
      identifier = clientIdentifier,
      localIpAddress = localIpAddress,
      ipAddressWithinVPN = vpnIpAddress,
      vpnUdpPort = localPort.toInt,
    )
  }
}
