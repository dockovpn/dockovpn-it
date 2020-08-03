
import com.alekslitvinenk.testcontainers.DockovpnContainer

import scala.concurrent.{Await, Future}

object Main extends App {
  
  DockovpnContainer().start()
  
  import scala.concurrent.duration._
  Await.result(Future.never, Duration.Inf)
}
