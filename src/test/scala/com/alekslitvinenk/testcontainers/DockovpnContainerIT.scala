package com.alekslitvinenk.testcontainers

import java.net.URL

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

import scala.collection.mutable
import scala.io.Source
import scala.util.Try

class DockovpnContainerIT extends AnyWordSpec with BeforeAndAfter with BeforeAndAfterAll {
  private var container: DockovpnContainer = _
  private val logs = mutable.ArrayBuffer[String]()
  
  "DockovpnContainer" should {
    "start successfully" in {
      val preLastLine = logs(logs.length - 2)
      val lastLine = logs.last
      
      // Check pre-last line content after timestamp and before URL
      preLastLine
        .substring(
          preLastLine.indexOf("Config server started"),
        ).strip should be("Config server started, download your client.ovpn config at http:///")
  
      // Check last line content after timestamp
      lastLine
        .substring(
          lastLine.indexOf("NOTE"),
        ).strip should be("NOTE: After you download your client config, http server will be shut down!")
    }
    
    "provide client config via http only once" in {
      val downloadConfigResult1 = downloadClientConfig()
      
      downloadConfigResult1.isSuccess should be(true)
      logs.last
        .substring(
          logs.last.indexOf("Config http"),
        ).strip should be("Config http server has been shut down")
  
      val downloadConfigResult2 = downloadClientConfig()
      
      downloadConfigResult2.isSuccess should be(false)
    }
  }
  
  private def downloadClientConfig(): Try[String] = Try {
    Source.fromURL(new URL(container.getConfigUrl))
  }.map(_.getLines().reduce(_ + _))
  
  override protected def afterAll(): Unit = {
    container.stop()
  }
  
  override protected def beforeAll(): Unit = {
    container = DockovpnContainer()
    container.withLogConsumer(outputFrame => logs += outputFrame.getUtf8String)
    container.start()
  }
}