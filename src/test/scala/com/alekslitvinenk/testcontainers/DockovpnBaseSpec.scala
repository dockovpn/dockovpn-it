package com.alekslitvinenk.testcontainers

import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils
import com.alekslitvinenk.testcontainers.dockovpn.DockovpnContainer
import org.scalatest.BeforeAndAfter
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.Network

import scala.collection.mutable

trait DockovpnBaseSpec extends AnyWordSpec with BeforeAndAfter {
  protected val dockerTagEnvVar = "DOCKER_IMAGE_TAG"
  protected val localhostTagEnvVar = "LOCAL_HOST"
  protected val runnerContainerEnvVar = "RUNNER_CONTAINER"
  protected val volumeRunnerContainerOpt = sys.env.get(runnerContainerEnvVar)
    .map(DockovpnContainerUtils.getMockedContainerByContainerName)
  
  protected var container: DockovpnContainer = _
  protected val logs = mutable.ArrayBuffer[String]()
  protected val tag = sys.env.getOrElse(dockerTagEnvVar, DockovpnContainer.Tags.latest)
  protected val localhostOpt = sys.env.get(localhostTagEnvVar).orElse(Some("localhost"))
  protected var network: Network = _
  
  before {
    import DockovpnContainer._
    
    Thread.sleep(1000)
    
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
    
    // make sure to clean the volume
  }
  
  after {
    container.stop()
    network.close()
  }
}
