package com.alekslitvinenk.testcontainers.aux

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnContainer
import org.testcontainers.containers.Container

import java.lang.reflect.{InvocationHandler, Method}
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

object DockovpnContainerUtils {
  
  class ContainerInvocationHandler(containerName: String) extends InvocationHandler {
    
    override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = containerName
  }
  
  implicit class DockovpnContainerHelper(container: DockovpnContainer) {
    
    def downloadConfigToTempDir(hostOpt: Option[String] = None): Try[String] = {
      container.downloadClientConfig(hostOpt).map { config =>
        val dir = Files.createTempDirectory("dockovpn-").toFile
        dir.deleteOnExit()
  
        val dirPath = dir.toPath
        println(s"Tempdir path: $dirPath")
  
        saveConfigToDir(dirPath, config)
      }
    }
    
    def downloadConfigToVolumeDir(pathStr: String, hostOpt: Option[String] = None): Try[String] = {
      container.downloadClientConfig(hostOpt).map { config =>
        val dirPath = Paths.get(pathStr)
        println(s"Dockovpn_data path: $dirPath")
        
        //Files.deleteIfExists(dirPath)
        
        saveConfigToDir(dirPath, config)
      }
    }
    
    private def saveConfigToDir(dirPath: Path, config: String): String = {
      val configFilePath = Files.createFile(dirPath.resolve("client.ovpn"))
      Files.write(configFilePath, config.getBytes)
      println(s"Config file path: $configFilePath")
  
      dirPath.toString
    }
  }
  
  def getMockedContainerByContainerName(name: String): Container[DockovpnContainer] = {
    import java.lang.reflect.Proxy
    
    val iClazz = classOf[Container[DockovpnContainer]]
    
    Proxy
      .newProxyInstance(
        iClazz.getClassLoader,
        Array[Class[_]](iClazz),
        new ContainerInvocationHandler(name))
      .asInstanceOf[Container[DockovpnContainer]]
  }
}
