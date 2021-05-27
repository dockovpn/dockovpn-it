package com.alekslitvinenk.testcontainers.dockovpn.loginterpreters

import com.alekslitvinenk.testcontainers.dockovpn.domain.ClientInfo.ClientInfoCallback

object ClientDisconnectInterpreter {
  def apply(callback: ClientInfoCallback) = new ClientDisconnectInterpreter(callback)
}

class ClientDisconnectInterpreter(callback: ClientInfoCallback) extends AbstractInterpreter(callback) {
  
}
