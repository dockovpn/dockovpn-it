package com.alekslitvinenk.testcontainers.command

import com.alekslitvinenk.testcontainers.DockovpnBaseSpec
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

class GenclientCmdSpec extends DockovpnBaseSpec {
  
  "Dockovpn 'genclient' command" should {
    "execute 'genclient' command successfully" in {
      // drain config generated at startup
      container.downloadClientConfig().isSuccess should be(true)
    
      // FixMe: due to GenericContainer implementation which waits for the command to complete and return exit code
      // we have to start command in another thread and then download generated config to make command return exit code
      val resFuture = Future { container.commands.generateClient }
    
      // need to give another thread a chance to send command to docker
      Thread.sleep(1000)
    
      container.downloadClientConfig().isSuccess should be(true)
    
      val res = Await.result(resFuture, 15.seconds)
    
      res.getExitCode should be (0)
    }
  }
}
