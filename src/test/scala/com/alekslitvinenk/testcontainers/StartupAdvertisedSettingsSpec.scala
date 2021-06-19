package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils._
import com.alekslitvinenk.testcontainers.dockovpn.DockovpnClientContainer
import org.scalatest.matchers.should.Matchers._
import org.testcontainers.containers.BindMode

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

class StartupAdvertisedSettingsSpec extends DockovpnBaseSpec {
  
  "Dockovpn" should {
    
    "start successfully" in {
      logs.takeRight(2).head should include ("Config server started, download your client.ovpn config at http://dockovpn")
      logs.last should include ("NOTE: After you download your client config, http server will be shut down")
    }
    
    "provide client config via http only once" in {
      container.downloadClientConfig().isSuccess should be(true)
      logs.last should include ("Config http server has been shut down")
      container.downloadClientConfig().isSuccess should be(false)
    }
    
    "accept connection from OpenVPN client with downloaded config" in {
      val (configDirPathOnHostMachine, configName) = downloadConfig()
      val clientContainer = createClient(configDirPathOnHostMachine, configName)
  
      container.getActiveClients should have length 1
      
      clientContainer.stop()
    }
    
    "accept connections from 2 OpenVPN clients that share same config" in {
      val (configDirPathOnHostMachine, configName) = downloadConfig()
  
      val startClientFuture1 = Future { createClient(configDirPathOnHostMachine, configName) }
      val startClientFuture2 = Future { createClient(configDirPathOnHostMachine, configName) }
      val allStartFuture = Future.reduceLeft(List(startClientFuture1.map(_ => ()), startClientFuture2.map(_ => ())))((_, _) => ())
      
      Await.ready(allStartFuture, 15.seconds)
  
      container.getActiveClients should have length 2
      
      val stopClientFuture1 = startClientFuture1.map(_.stop())
      val stopClientFuture2 = startClientFuture2.map(_.stop())
      val allStopFuture = Future.reduceLeft(List(stopClientFuture1, stopClientFuture2))((_, _) => ())
  
      Await.ready(allStopFuture, 15.seconds)
    }
  }
  
  private def downloadConfig(): (String, String) = volumeRunnerContainerOpt
    .fold(container.downloadConfigToTempDir(localhostOpt)) { _ =>
      container.downloadConfigToVolumeDir(DockovpnClientContainer.getConfigDir, localhostOpt)
    }.get
  
  private def createClient(configDirPath: String, configName: String): DockovpnClientContainer = {
    val clientContainer = DockovpnClientContainer(configName)
    volumeRunnerContainerOpt
      .fold(clientContainer.withFileSystemBind(configDirPath, DockovpnClientContainer.getConfigDir)) { container =>
        clientContainer.withVolumesFrom(container, BindMode.READ_ONLY)
      }
    
    clientContainer.withNetwork(network)
    clientContainer.start()
  
    clientContainer
  }
}
