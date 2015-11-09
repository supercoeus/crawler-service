package crawler.util.persist

import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures}
import crawler.SystemUtils

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Try

/**
 * CassandraPersists
 * Created by yangjing on 15-11-6.
 */
object CassandraPersists {

  val cluster =
    Cluster.builder().addContactPoints(SystemUtils.crawlerConfig.getStringList("cassandra.nodes").asScala: _*).build()

  def userType(keyspace: String, userType: String): UserType =
    cluster.getMetadata.getKeyspace(keyspace).getUserType(userType)

  def using[R](keyspace: String)(func: Session => R): R = {
    val session = cluster.connect(keyspace)
    try {
      func(session)
    } finally {
      session.closeAsync()
    }
  }

  def execute[R](resultSetFuture: ResultSetFuture)(func: ResultSet => R)(implicit ec: ExecutionContextExecutor): Future[R] = {
    val promise = Promise[R]()
    Futures.addCallback(
      resultSetFuture,
      new FutureCallback[ResultSet] {
        override def onFailure(t: Throwable): Unit = {
          promise.failure(t)
        }

        override def onSuccess(rs: ResultSet): Unit = {
          promise.complete(Try(func(rs)))
        }
      },
      ec)
    promise.future
  }
}
