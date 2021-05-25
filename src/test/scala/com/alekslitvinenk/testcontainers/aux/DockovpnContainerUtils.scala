package com.alekslitvinenk.testcontainers.aux

import com.alekslitvinenk.testcontainers.DockovpnContainer

import java.nio.file.Files
import scala.util.Try

object DockovpnContainerUtils {
  
  implicit class DockovpnContainerHelper(container: DockovpnContainer) {
    
    def downloadConfigToTempDir(hostOpt: Option[String] = None): Try[String] = {
      
      container.downloadClientConfig(hostOpt).map { config =>
        val dir = Files.createTempDirectory("dockovpn-").toFile
        dir.deleteOnExit()
  
        val dirPath = dir.toPath
        println(s"Tempdir path: $dirPath")
  
        val configFilePath = Files.createFile(dirPath.resolve("client.ovpn"))
        Files.write(configFilePath, config.getBytes)
        println(s"Config file path: $configFilePath")
  
        dirPath.toString
      }
    }
  }
}
