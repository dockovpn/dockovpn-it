package com.alekslitvinenk.testcontainers.aux

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnContainer
import org.testcontainers.containers.Container

import java.lang.reflect.{InvocationHandler, Method}
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Try

object DockovpnContainerUtils {
  
  private lazy val configCounter: AtomicInteger = new AtomicInteger(0)
  
  class ContainerInvocationHandler(containerName: String) extends InvocationHandler {
    
    override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef =
      method.getName match {
        case "getContainerName" => containerName
        case _ => ???
      }
  }
  
  implicit class DockovpnContainerHelper(container: DockovpnContainer) {
  
    /**
     * Download config to a temp directory on a local machine
     * @param hostOpt optional network host name of Dockovpn container
     * @return
     */
    def downloadConfigToTempDir(hostOpt: Option[String] = None): Try[(String, String)] = {
      container.downloadClientConfig(hostOpt).map { config =>
        val dir = Files.createTempDirectory("dockovpn-").toFile
        dir.deleteOnExit()
  
        val dirPath = dir.toPath
        println(s"Tempdir path: $dirPath")
  
        saveConfigToDir(dirPath, config)
      }
    }
  
    /**
     * Download config to a dir on a mounted volume of a Dockovpn container
     * @param pathStr dir on a mounted volume
     * @param hostOpt optional network host name of Dockovpn container
     * @return
     */
    def downloadConfigToVolumeDir(pathStr: String, hostOpt: Option[String] = None): Try[(String, String)] = {
      container.downloadClientConfig(hostOpt).map { config =>
        val dirPath = Paths.get(pathStr)
        println(s"Dockovpn_data path: $dirPath")
        
        saveConfigToDir(dirPath, config)
      }
    }
    
    private def saveConfigToDir(dirPath: Path, config: String): (String, String) = {
      val configName = s"client${configCounter.incrementAndGet()}.ovpn"
      
      val configFilePath = Files.createFile(dirPath.resolve(configName))
      Files.write(configFilePath, config.getBytes)
      println(s"Config file path: $configFilePath")
  
      (dirPath.toString, configName)
    }
  }
  
  /**
   * Return the proxy wrapper for parent container
   * @param name parent container name
   * @return
   */
  def getParentContainerProxyByName(name: String): Container[DockovpnContainer] = {
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
