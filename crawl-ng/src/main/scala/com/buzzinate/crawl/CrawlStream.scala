package com.buzzinate.crawl

import com.twitter.util.Future
import scala.collection.mutable.ListBuffer
import com.buzzinate.stream._
import com.twitter.util.Promise
import org.jsoup.Jsoup
import com.twitter.util.FuturePool
import collection.JavaConverters._
import com.buzzinate.http.Http
import com.buzzinate.http.PolicyHttpScheduler
import java.util.concurrent.Executors

trait UrlEntry {
  val url: String
}

trait UrlSource[E <: UrlEntry] extends Source[E] {
  def submit(ues: Iterable[E]): Unit
}

case class DumyUrlEntry(url: String) extends UrlEntry

trait DumyUrlSource[E <: UrlEntry] extends UrlSource[E] {
  def offer(e: E): Unit
  
  def submit(ues: Iterable[E]) = {
//    println("submit new: " + ues.map(ue => ue.url))
    ues foreach offer
  }
}

object CrawlStream {
  def from[E <: UrlEntry](urlsrc: UrlSource[E]): FutureStream[E] = {
    FutureStream.from(urlsrc)
  }
  
  def main(args: Array[String]): Unit = {
    val client = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 1, 10)
    
//    val urls = List("http://tianjin.51chudui.com/", "http://shanghai.51chudui.com/", "http://www.cnbeta.com/articles/231120.htm")
//    
//    urls foreach { url =>
//      client.get(url) onSuccess { resp =>
//        val hr = resp.toHtml(url)
//        val doc = Jsoup.parse(hr.html, url)
//        println(url + " => " + doc.title + hr.lastRedirectedUri)
//        println(doc.text())
//      }
//    }
//    if (true) return
    val urls = List("http://shanghai.51chudui.com/Class/fandian/20133/361709.html", "http://www.cnbeta.com/articles/231120.htm")
    val src = new MemorySource(urls.map(url => DumyUrlEntry(url))) with DumyUrlSource[DumyUrlEntry]
    
    
    val pages = from(src).mapFuture { case DumyUrlEntry(url) =>
      Future.value(url) join client.get(url)
    }.map { case (url, resp) =>
      val hr = resp.toHtml(url)
      val doc = Jsoup.parse(hr.html, url)
      val title = doc.title
      println(url + hr.lastRedirectedUri.map(x => "/" + x).getOrElse("") +  " => " + title)
      (url, title, hr)
    }.batch(2, 20) { list =>
      println("store " + list.map(x => x._1 + "/" + x._2))
    }
    
    pages.flatMap { case (url, title, hr) =>
      val doc = Jsoup.parse(hr.html, url)
      doc.getElementsByTag("a").asScala.map(e => e.absUrl("href")).filter(x => x.startsWith("http://") && (x.contains("51chudui.com") || x.contains("cnbeta.com")))
    }.batch(10, 200) { sublinks =>
      src.submit(sublinks.map(url => DumyUrlEntry(url)))
    }.commit
    
    src.start
  }
}