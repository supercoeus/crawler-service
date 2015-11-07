package crawler.news.service

import akka.actor.Props
import crawler.news.NewsUtils
import crawler.news.commands.RequestSearchNews
import crawler.news.service.actors.{NewsJob, PersistActor}
import crawler.util.actors.MetricActor

/**
 * News Supervisor
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class NewsMaster extends MetricActor {
  val persistActor = context.actorOf(PersistActor.props, PersistActor.actorName)

  override val metricReceive: Receive = {
    case RequestSearchNews(sources, msg) =>
      val doSender = sender()
      val newsJob = context.actorOf(NewsJob.props(sources, doSender), "news-" + NewsUtils.getIndent)
      newsJob ! msg
  }
}

object NewsMaster {
  val actorName = "news-master"

  def props = Props(new NewsMaster)
}