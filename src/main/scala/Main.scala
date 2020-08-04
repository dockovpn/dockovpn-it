
import java.net.URL

import com.alekslitvinenk.testcontainers.DockovpnContainer

import scala.io.Source
import scala.util.Try

object Main extends App {
  
  val container = DockovpnContainer()
  container.start()
  // Should
  
  // Return config file when config endpoint accessed first time
  // also check there's log message that http server stopped
  val res1 = Try {
    Source.fromURL(new URL(container.getConfigUrl))
  }.map(_.getLines().reduce(_ + _)).toOption
  println(res1)
  
  // Return nothing when config endpoint accessed second time
  val res2 = Try {
    Source.fromURL(new URL(container.getConfigUrl))
  }.map(_.getLines().reduce(_ + _)).toOption
  println(res2)
}
