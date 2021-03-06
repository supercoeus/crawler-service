package crawler.testsuite

import crawler.SystemUtils
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

/**
 * Created by yangjing on 15-11-4.
 */
abstract class ServiceSpec
  extends WordSpec
  with BeforeAndAfterAll
  with MustMatchers
  with OptionValues
  with EitherValues
  with ScalaFutures {

  implicit def system = SystemUtils.system
  implicit def materializer = SystemUtils.materializer
  implicit def dispatcher = system.dispatcher
  implicit val defaultPatience = PatienceConfig(Span(30, Seconds))

  override protected def afterAll(): Unit = {
    SystemUtils.shutdown()
  }

}
