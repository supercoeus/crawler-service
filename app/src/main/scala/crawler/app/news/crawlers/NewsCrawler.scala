package crawler.app.news.crawlers

import com.typesafe.scalalogging.LazyLogging
import crawler.app.news.NewsUtils
import crawler.enums.ItemSource
import crawler.model.{NewsPageItem, SearchResult}
import crawler.util.Crawler
import crawler.util.news.contextextractor.ContentExtractor
import org.jsoup.helper.DataUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * 新闻爬虫
  * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
  */
abstract class NewsCrawler(val newsSource: ItemSource.Value) extends Crawler with LazyLogging {
  /**
    * 抓取搜索页
    *
    * @param key 搜索关键词
    * @return
    */
  def fetchItemList(key: String)(implicit ec: ExecutionContext): Future[SearchResult]

  /**
    * 抓取新闻详情页
    *
    * @param url 网页链接
    * @return
    */
  def fetchNewsItem(url: String)(implicit ec: ExecutionContext): Future[NewsPageItem] = {
    fetchPage(url).map { resp =>
      val in = resp.getResponseBodyAsStream
      val doc = DataUtil.load(in, null, NewsUtils.uriToBaseUri(url))
      val src = doc.toString
      //      try {
      val news = ContentExtractor.getNewsByDoc(doc)
      NewsPageItem(url, src, /*news.getTitle, news.getTime,*/ news.getContent)
      //      } catch {
      //        case e: Exception =>
      //          logger.warn(s"$url context extractor", e)
      //          NewsPageItem(url, src, /*"", "",*/ "")
      //      }
    }
  }

}

object NewsCrawler {
  private var _newsCrawler = Map.empty[ItemSource.Value, NewsCrawler]

  def registerCrawler(source: ItemSource.Value, newsCrawler: NewsCrawler): Unit = {
    _newsCrawler = _newsCrawler + (source -> newsCrawler)
  }

  def getCrawler(source: ItemSource.Value): Option[NewsCrawler] = _newsCrawler.get(source)

}
