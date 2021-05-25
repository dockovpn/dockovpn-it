package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils._
import com.alekslitvinenk.testcontainers.domain.ClientInfo
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.Network

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DockovpnDefaultConfigIT extends AnyWordSpec with BeforeAndAfter {
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
      
      logs.takeRight(2).head should include ("Peer Connection Initiated with [AF_INET]")
      logs.last should include ("MULTI_sva: pool returned IPv4=")
      
      clientContainer.stop()
    }
    
    "accept connections from 2 OpenVPN clients that share same config" in {
      val configDirPath = container.downloadConfigToTempDir(Some("localhost")).get
      
      def clientConnected(clientInfo: ClientInfo): Unit = {
        println(clientInfo)
      }
  
      container.onClientConnected(clientConnected)
  
      val futureClient1 = Future { createClient(configDirPath) }
      val futureClient2 = Future { createClient(configDirPath) }
      
      val allStartFuture = Future.reduceLeft(List(futureClient1.map(_ => ()), futureClient2.map(_ => ())))((_, _) => ())
      
      Await.ready(allStartFuture, Duration.Inf)
      
      val s1 = futureClient1.map(_.stop())
      val s2 = futureClient2.map(_.stop())
      
      val allStopFuture = Future.reduceLeft(List(s1, s2))((_, _) => ())
  
      Await.ready(allStopFuture, Duration.Inf)
    }
    
    "execute 'version' command successfully" in {
    
    }
  
    "execute 'genclient' command successfully" in {
    
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
