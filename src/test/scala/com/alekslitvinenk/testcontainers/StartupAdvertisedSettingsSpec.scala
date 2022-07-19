package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils._
import com.alekslitvinenk.testcontainers.dockovpn.DockovpnClientContainer
import org.scalatest.matchers.should.Matchers._
import org.testcontainers.containers.BindMode

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}

class StartupAdvertisedSettingsSpec extends DockovpnBaseSpec {
  
  "Dockovpn" should {
    
    "start successfully" in {
      logs.takeRight(2).head should include ("Config server started, download your client.ovpn config at http://dockovpn")
      logs.last should include ("NOTE: After you download your client config, http server will be shut down")
    }
    
    "provide client config via http only once" in {
      container.downloadClientConfig().isSuccess should be(true)
      // Let's give some time for container to produce logs
      Thread.sleep(sleepTimeout)
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
      val nClients: Int = 2
      val startClientsFutures: Seq[Future[DockovpnClientContainer]] =
        for (_ <- 1 to nClients) yield Future { createClient(configDirPathOnHostMachine, configName) }
      
      Await.ready(Future.sequence(startClientsFutures), defaultTimeout)
  
      container.getActiveClients should have length nClients
  
      val stopClientsFutures: Seq[Future[Unit]] = startClientsFutures.map(_.map(_.stop()))
  
      Await.ready(Future.sequence(stopClientsFutures), defaultTimeout)
    }
  }
  
  private def downloadConfig(): (String, String) = parentContainerWithVolumesOpt
    .fold(container.downloadConfigToTempDir(containerHostOpt)) { _ =>
      container.downloadConfigToVolumeDir(DockovpnClientContainer.getConfigDir, containerHostOpt)
    }.get
  
  private def createClient(configDirPath: String, configName: String): DockovpnClientContainer = {
    val clientContainer = DockovpnClientContainer(configName)
    parentContainerWithVolumesOpt
      .fold { clientContainer.withFileSystemBind(configDirPath, DockovpnClientContainer.getConfigDir)
      } { container =>
        clientContainer.withVolumesFrom(container, BindMode.READ_ONLY)
      }
    
    clientContainer.withNetwork(network)
    clientContainer.start()
  
    clientContainer
  }
}
