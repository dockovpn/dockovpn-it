package com.alekslitvinenk.testcontainers.dockovpn.domain

import java.util.Date

object ClientInfo {
  type ClientInfoCallback = ClientInfo => Unit
}

/**
 * General info about Dockovpn client
 * @param timeConnected
 * @param timeDisconnected
 * @param identifier
 * @param localIpAddress
 * @param vpnUdpPort
 * @param ipAddressWithinVPN
 */
case class ClientInfo(
  timeConnected: Date,
  timeDisconnected: Option[Date] = None,
  identifier: String,
  localIpAddress: String,
  vpnUdpPort: Int,
  ipAddressWithinVPN: String,
)
