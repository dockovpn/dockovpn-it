package com.alekslitvinenk.testcontainers.dockovpn.commands

import com.alekslitvinenk.testcontainers.dockovpn.DockovpnContainer
import org.testcontainers.containers.Container.ExecResult

/**
 * Set of Dockovpn specific commands to execute in running container
 * @param container DockOvpn container
 */
class DockovpnCommands(container: DockovpnContainer) {
  /**
   * Fetches version of currently running container
   * @return
   */
  def getVersion(): ExecResult = container.execInContainer("./version.sh")
  
  /**
   * Generates new client configuration in running container
   * @return
   */
  def generateClient(): ExecResult = container.execInContainer("./genclient.sh")
  
  /**
   * Removes client configuration by Id
   * @param clientId client Id
   * @return
   */
  def removeClient(clientId: String): ExecResult = container.execInContainer(s"./rmclient.sh $clientId")
}

object DockovpnCommands {
  def apply(container: DockovpnContainer): DockovpnCommands = new DockovpnCommands(container)
}
