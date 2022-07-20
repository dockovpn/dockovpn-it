package com.alekslitvinenk.testcontainers.command

import com.alekslitvinenk.testcontainers.DockovpnBaseSpec
import com.alekslitvinenk.testcontainers.aux.ConfigSupport
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}

class RmClientCmdSpec extends DockovpnBaseSpec with ConfigSupport {
  "Dockovpn 'rmclient' command" should {
    
    "delete client configuration successfully if it exists" in {
      // drain config generated at startup
      val (configDirPath, configName) = downloadConfig()
      val clientId = getClientIdFromConfig(configDirPath, configName)
      
      val resFuture = Future { container.commands.removeClient(clientId) }
      val res = Await.result(resFuture, defaultTimeout)
      res.getExitCode should be(0)
      
      val clientTry = createClient(configDirPath, configName)
      clientTry.isSuccess should be(false)
  
      // container should not accept connection from removed client and thus
      // should not add it to activeClients
      container.getActiveClients.length should be(0)
      
      clientTry.map(_.stop())
    }
    
    "fail if client with given id doesn't exist" in {
      val clientId = "invalidClientId"
  
      val resFuture = Future { container.commands.removeClient(clientId) }
      val res = Await.result(resFuture, defaultTimeout)
      
      res.getStderr.stripLineEnd should include("Unable to revoke as the input file is not a valid certificate")
    }
  }
}
