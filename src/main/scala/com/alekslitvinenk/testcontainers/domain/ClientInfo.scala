package com.alekslitvinenk.testcontainers.domain

import java.util.Date

object ClientInfo {
  type ClientInfoCallback = ClientInfo => Unit
}

case class ClientInfo(
  timeConnected: Date,
  timeDisconnected: Option[Date] = None,
  identifier: String,
  localIpAddress: String,
  vpnUdpPort: Int,
  ipAddressWithinVPN: String,
)
