package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils._
import com.alekslitvinenk.testcontainers.dockovpn.{DockovpnClientContainer, DockovpnContainer}
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.Network

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DockovpnAdvertisedSettingsIT extends AnyWordSpec with BeforeAndAfter {
  private val dockerTagEnvVar = "DOCKER_IMAGE_TAG"
  private var container: DockovpnContainer = _
  private val logs = mutable.ArrayBuffer[String]()
  private val tag = sys.env.getOrElse(dockerTagEnvVar, DockovpnContainer.Tags.latest)
  private var network: Network = _
  
  before {
    import DockovpnContainer._
    
    logs.clear()
    
    network = Network.newNetwork()
    
    val containerAlias = "dockovpn"
    
    container = DockovpnContainer(tag)
    container.withLogConsumer(outputFrame => logs += outputFrame.getUtf8String)
    container.withEnv(Envs.hostAddress, containerAlias)
    container.withNetwork(network)
    container.withNetworkAliases(containerAlias)
    container.start()
    // FixMe
    container.onClientConnected(_ => ())
  }
  
  after {
    container.stop()
    network.close()
  }
  
  "DockovpnContainer" should {
    
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
      val configDirPath = container.downloadConfigToTempDir(Some("localhost")).get
      val clientContainer = createClient(configDirPath)
  
      container.getActiveClients should have length 1
      
      clientContainer.stop()
    }
    
    "accept connections from 2 OpenVPN clients that share same config" in {
      val configDirPath = container.downloadConfigToTempDir(Some("localhost")).get
  
      val startClientFuture1 = Future { createClient(configDirPath) }
      val startClientFuture2 = Future { createClient(configDirPath) }
      val allStartFuture = Future.reduceLeft(List(startClientFuture1.map(_ => ()), startClientFuture2.map(_ => ())))((_, _) => ())
      
      Await.ready(allStartFuture, Duration.Inf)
  
      container.getActiveClients should have length 2
      
      val stopClientFuture1 = startClientFuture1.map(_.stop())
      val stopClientFuture2 = startClientFuture2.map(_.stop())
      val allStopFuture = Future.reduceLeft(List(stopClientFuture1, stopClientFuture2))((_, _) => ())
  
      Await.ready(allStopFuture, Duration.Inf)
    }
    
    "execute 'version' command successfully" in {
      val res = container.commands.getVersion
      
      res.getExitCode should be(0)
      
      val date = "[A-Za-z]{3}\\s[A-Za-z]{3}\\s\\d{1,2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{4}"
      val app = "Dockovpn"
      val version = "v\\d{1}\\.\\d{1}\\.\\d{1}"
      res.getStdout.stripLineEnd should fullyMatch regex s"^$date\\s$app\\s$version"
    }
  
    "execute 'genclient' command successfully" in {
      // drain config generated at startup
      container.downloadClientConfig(Some("localhost"))
      
      val resFuture = Future { container.commands.generateClient }
      
      // need to give another thread a chance to send command to docker
      Thread.sleep(1000)
  
      container.downloadClientConfig(Some("localhost")).isSuccess should be(true)
      
      val res = Await.result(resFuture, Duration.Inf)
      
      res.getExitCode should be (0)
    }
  }
  
  private def createClient(configDirPath: String): DockovpnClientContainer = {
    val clientContainer = DockovpnClientContainer()
    clientContainer.withFileSystemBind(configDirPath, clientContainer.getConfigDir)
    clientContainer.withNetwork(network)
    clientContainer.start()
  
    clientContainer
  }
}
