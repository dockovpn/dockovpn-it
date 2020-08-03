
import java.time._

import com.dimafeng.testcontainers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.{FixedHostPortGenericContainer, InternetProtocol}

import scala.concurrent.{Await, Future}

object Main extends App {
  
  private val httpPort = 8080
  private val udpPort = 1194
  private val dockovpn = "alekslitvinenk/openvpn"
  private val logEntryRegEx = ".*Config\\sserver\\sstarted.*"
  private val startupTimeout = Duration.ofSeconds(10)
  private val container = new FixedHostPortGenericContainer(dockovpn)
  container.withFixedExposedPort(httpPort, httpPort, InternetProtocol.TCP)
  container.withFixedExposedPort(udpPort, udpPort, InternetProtocol.UDP)
  container.withPrivilegedMode(true)
  container.waitingFor(
    Wait.forLogMessage(logEntryRegEx, 1)
      .withStartupTimeout(startupTimeout)
  )
  container.start()
  
  val mySQLContainer = MySQLContainer()
  
  import scala.concurrent.duration._
  Await.result(Future.never, Duration.Inf)
}
