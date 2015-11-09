package crawler.news.service.actors

import akka.actor.{ActorRef, Cancellable, PoisonPill, Props}
import crawler.news.commands._
import crawler.news.enums.{SearchMethod, NewsSource}
import crawler.news.model.NewsResult
import crawler.news.service.NewsMaster
import crawler.util.actors.MetricActor

import scala.concurrent.duration.FiniteDuration

/**
 * 新闻job
 * @param source 搜索源
 * @param method 搜索方式
 * @param key 搜索关键词
 * @param duration 持续时间，到期后向未获取完新闻数据向客户端返回Timeout。children actor继续业务处理
 * @param reqSender 请求actor
 */
class NewsSourceJob(source: NewsSource.Value,
                    method: SearchMethod.Value,
                    key: String,
                    duration: FiniteDuration,
                    reqSender: ActorRef) extends MetricActor {

  @volatile var _newsResult = NewsResult(source, "", 0, Nil)
  @volatile var _isTimeout: Boolean = false
  @volatile var _notCompleteItemPageActorNames = Seq.empty[String]
  @volatile var _cancelableSchedule: Cancellable = _

  import context.dispatcher

  override def metricPreStart(): Unit = {
    // 定义超时时间
    _cancelableSchedule = context.system.scheduler.scheduleOnce(duration, self, SearchTimeout)
  }

  override def metricPostStop(): Unit = {
    if (!_cancelableSchedule.isCancelled) {
      _cancelableSchedule.cancel()
    }

    if (null != _newsResult && _newsResult.count > 0) {
      context.actorSelection(context.system / NewsMaster.actorName / PersistActor.actorName) ! _newsResult
    } else {
      logger.warn("未获取到相关数据: " + _newsResult.error)
    }
  }

  override val metricReceive: Receive = {
    case s@StartSearchNews =>
      val searchPage = context.actorOf(SearchPageWorker.props(source, key), "page")
      searchPage ! StartFetchSearchPage

    case SearchResult(newsResult) =>
      _newsResult = newsResult
      method match {
        case SearchMethod.F if _newsResult.count > 0 => // 需要抓取详情内容
          _notCompleteItemPageActorNames = newsResult.news.zipWithIndex.map { case (item, idx) =>
            val childName = "item-" + idx
            val itemPage = context.actorOf(ItemPageWorker.props(source, item), childName)
            itemPage ! StartFetchItemPage
            childName
          }

        case _ => // SearchMethod.S => // 只抓取摘要
          if (!_isTimeout) {
            reqSender ! _newsResult
          }
          self ! PoisonPill
      }

    case ItemPageResult(result) =>
      val doSender = sender()
      _notCompleteItemPageActorNames = _notCompleteItemPageActorNames.filterNot(_ == doSender.path.name)
      result match {
        case Left(errMsg) =>
        // TODO 解析新闻详情页失败！

        case Right(pageItem) =>
          // 更新 result.news
          val news = _newsResult.news.map {
            case oldItem if oldItem.url == pageItem.url =>
              oldItem.copy(content = pageItem.content)
            case oldItem => oldItem
          }
          _newsResult = _newsResult.copy(news = news)
      }

      if (_notCompleteItemPageActorNames.isEmpty) {
        if (!_isTimeout) {
          reqSender ! _newsResult
        }
        self ! PoisonPill
      }

    case SearchTimeout =>
      _isTimeout = true

      // 此时向调用客户端返回已存在的数据，但实际的新闻抓取流程仍将继续
      reqSender ! _newsResult //Left(new AskTimeoutException("搜索超时"))

    case SearchPageFailure(e) =>
      if (!_isTimeout) {
        reqSender ! NewsResult(source, key, -1, Nil, Some(e.getLocalizedMessage))
      }
      self ! PoisonPill
  }

}

object NewsSourceJob {
  def props(source: NewsSource.Value,
            method: SearchMethod.Value,
            key: String,
            duration: FiniteDuration,
            reqSender: ActorRef) =
    Props(new NewsSourceJob(source, method, key, duration, reqSender))
}
