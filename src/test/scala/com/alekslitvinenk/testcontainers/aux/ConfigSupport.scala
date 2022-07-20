package com.alekslitvinenk.testcontainers.aux

import com.alekslitvinenk.testcontainers.DockovpnBaseSpec
import com.alekslitvinenk.testcontainers.aux.DockovpnContainerUtils.DockovpnContainerHelper
import com.alekslitvinenk.testcontainers.dockovpn.DockovpnClientContainer
import org.testcontainers.containers.BindMode

import scala.io.Source
import scala.util.{Failure, Try}

trait ConfigSupport { self: DockovpnBaseSpec =>
  
  protected def downloadConfig(): (String, String) = parentContainerWithVolumesOpt
    .fold(container.downloadConfigToTempDir(containerHostOpt)) { _ =>
      container.downloadConfigToVolumeDir(DockovpnClientContainer.getConfigDir, containerHostOpt)
    }.get
  
  protected def createClient(configDirPath: String, configName: String): Try[DockovpnClientContainer] = {
    val clientContainer = DockovpnClientContainer(configName)
    parentContainerWithVolumesOpt
      .fold { clientContainer.withFileSystemBind(configDirPath, DockovpnClientContainer.getConfigDir)
      } { container =>
        clientContainer.withVolumesFrom(container, BindMode.READ_ONLY)
      }
    
    clientContainer.withNetwork(network)
    
    // FixMe: Container readiness check should be performed based on more loose regex pattern
    Try {
      clientContainer.start()
    } recoverWith { ex: Throwable =>
      clientContainer.stop()
      Failure(ex)
    } map(_ => clientContainer)
  }
  
  protected def getClientIdFromConfig(configDirPath: String, configName: String): String = {
    val source = Source.fromFile(s"$configDirPath/$configName")
    val l: List[String] = source.getLines().foldLeft(List.empty[String])((l, e) => e :: l)
    
    l.head.split(" ")(1)
  }
}
