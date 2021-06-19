package com.alekslitvinenk.testcontainers.command

import com.alekslitvinenk.testcontainers.DockovpnBaseSpec
import org.scalatest.matchers.should.Matchers._

class VersionCmdSpec extends DockovpnBaseSpec {
  
  "Dockovpn 'version' command" should {
    "run successfully" in {
      val res = container.commands.getVersion
    
      res.getExitCode should be(0)
    
      val date = "[A-Za-z]{3}\\s[A-Za-z]{3}\\s\\d{1,2}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{4}"
      val app = "Dockovpn"
      val version = "v\\d{1}\\.\\d{1}\\.\\d{1}"
      res.getStdout.stripLineEnd should fullyMatch regex s"^$date\\s$app\\s$version"
    }
  }
}
