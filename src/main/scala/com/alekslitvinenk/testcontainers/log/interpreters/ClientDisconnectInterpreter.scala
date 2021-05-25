package com.alekslitvinenk.testcontainers.log.interpreters

import com.alekslitvinenk.testcontainers.domain.ClientInfo.ClientInfoCallback

object ClientDisconnectInterpreter {
  def apply(callback: ClientInfoCallback) = new ClientDisconnectInterpreter(callback)
}

class ClientDisconnectInterpreter(callback    : ClientInfoCallback) extends AbstractInterpreter(callback){
  
}
