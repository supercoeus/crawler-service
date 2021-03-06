package crawler.module.news.model

import java.time.LocalDateTime

import crawler.module.news.enums.ItemSource

/**
 * 搜索结果
 * Created by yangjing on 15-11-3.
 */
case class SearchResult(source: ItemSource.Value,
                        key: String,
                        time: LocalDateTime,
                        count: Int,
                        news: Seq[NewsItem],
                        error: Option[String] = None)
