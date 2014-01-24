package com.buzzinate.keywords.util

import org.jsoup.nodes.Document
import java.util.regex.Pattern
import org.jsoup.nodes.Element
import com.abahgat.suffixtree.GeneralizedSuffixTree
import com.buzzinate.nlp.segment.AtomSplit
import collection.JavaConverters._
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.apache.commons.lang.StringUtils
import com.buzzinate.nlp.util.TextUtil
import com.buzzinate.flash.FlashThumbnail
import scala.collection.mutable.ListBuffer
import org.apache.commons.io.filefilter.RegexFileFilter
import com.twitter.util.Future
import com.buzzinate.http.HttpScheduler

case class NodeInfo(imgscore: Option[(Element, Double)], nImgs: Int, nWords: Int, nTitleWords: Int, nLinkWords: Int, nPuncs: Int) {
  def score(): Double = {
    val textDensity = (nWords - nLinkWords) / (1d + nWords)
    ((1 + math.log(1 + nPuncs)) * (1 + math.log(1 + nTitleWords)) - 1) * textDensity / (1 + math.log(1 + nImgs))
  }
}

object ThumbnailExtractor {
  
  def extractThumbnail(client: HttpScheduler, doc: Document, title: String, url: String): Option[String] = {
    val sf = new GeneralizedSuffixTree
    for (s <- AtomSplit.splitSnippets(title).asScala) {
      sf.put(s)
    }
    val NodeInfo(bestImg, _, _, _, _, _) = calcNodeInfo(sf, doc.body, false, url)

    bestImg map {
      case (img, _) =>
        img.tagName match {
          case "img" => {
            if (img.hasAttr("width") && parseInt(img.attr("width"), 200) < 200 || img.hasAttr("height") && parseInt(img.attr("height"), 150) < 150) None
            Some(img.absUrl("src"))
          }
          case "embed" => {
            FlashThumbnail.getThumbnail(client, img.absUrl("src")).map { src =>
              if (src == null) None else Some(src)
            }.get
          }
        }
    } getOrElse(None)
  }

  private def calcNodeInfo(sf: GeneralizedSuffixTree, root: Node, inLink: Boolean, url: String): NodeInfo = {
    var nImgs = 0
    var nWords = 0
    var nTitleWords = 0
    var nLinkWords = 0
    var nPuncs = 0
    var imgs = new ListBuffer[Element]
    var bestImg: Element = null
    var bestScore = 0d
    root match {
      case tn: TextNode => {
        val text = StringUtils.replace(tn.text, "\u00a0", " ").trim
        if (text.length > 0) {
          if (inLink) nLinkWords += TextUtil.countNumWords(text)
          nWords += TextUtil.countNumWords(text)
          nPuncs += TextUtil.countPuncs(text)
          sf.searchMax(text).asScala foreach { w =>
            if (w.length >= 1) nTitleWords += TextUtil.countNumWords(w)
          }
        }
      }
      case e: Element => {
        if (e.tagName == "img" || e.tagName == "embed") {
          nImgs += 1
          imgs += e
        } else {
          val isLink = inLink || (e.tagName == "a")
          e.childNodes.asScala foreach { c =>
            val ni = calcNodeInfo(sf, c, isLink, url)
            nImgs += ni.nImgs
            nWords += ni.nWords
            nTitleWords += ni.nTitleWords
            nLinkWords += ni.nLinkWords
            nPuncs += ni.nPuncs
            ni.imgscore foreach {
              case (img, score) =>
                imgs += img
                if (score > bestScore) {
                  bestScore = score
                  bestImg = img
                }
            }
          }
        }
      }
      case _ => {}
    }

    val ni = NodeInfo(None, nImgs, nWords, nTitleWords, nLinkWords, nPuncs)
    val niscore = ni.score
    imgs foreach { img =>
      val score = scoreThumbnail(img, sf, url)
      if (score > 3) {
        val imgscore = (1 + score) * (0.1 + niscore)
        if (imgscore > bestScore) {
          bestScore = imgscore
          bestImg = img
        }
      }
    }

    val resultImg = if (bestImg == null) None else Some((bestImg, bestScore))
    NodeInfo(resultImg, nImgs, nWords, nTitleWords, nLinkWords, nPuncs)
  }

  private def scoreThumbnail(img: Element, sf: GeneralizedSuffixTree, url: String): Double = {
    val parent = img.parent()
    val ptag = parent.tagName()

    val src = img.absUrl("src").toLowerCase()
    var score = 0d     
    
    if (ptag.equalsIgnoreCase("a")) {
      val href = parent.absUrl("href")
      val start = if (url.startsWith("http://")) 7 else 0
      var suffix = if (url.substring(start).contains("/")) url.substring(url.indexOf("/", start)).trim() else ""
      if(suffix.contains(".")) suffix = suffix.substring(0,suffix.indexOf("."))
      if(suffix.contains("#")) suffix = suffix.substring(0,suffix.indexOf("#"))
      if(suffix.matches(".*?_\\d{1,2}")) suffix = suffix.substring(0,suffix.lastIndexOf("_"))
      if (href == null) {
        score += 5
      } else {
        if (url.contains("gmw.cn")) { //光明网图片问题
          val p = "content_\\d+".r
          val matchs = p.findFirstIn(url)
          if (matchs != None) suffix = matchs.get
        }
        if(href.contains(suffix) || href.toLowerCase().matches(".+(jpg|png|gif|jpeg)")) score += 10    
        else if(href.contains("picshow")){//图片展示
          score += 3
        } else if (!url.contains("nen.com.cn") && !url.contains("abang.com")) score -= 10
      }
    } else if (ptag.equalsIgnoreCase("button") || ptag.contains("script")) score -= 10    
    else if (ptag.equals("p")) score += 5
    else if(ptag.equals("td") && parent.hasAttr("height") && Integer.parseInt(parent.attr("height")) < 40) score -= 5
    
    if (img.hasAttr("alt")) score += 1
    if (!src.endsWith("gif")) score += 2 else score -= 5
    if (src.contains("upload")) score += 4
    if (src.contains("attachment")) score += 4
    if (src.contains("photo")) score += 4
    if(src.contains("button")) score -= 5
    if(src.contains("validcode")) score -=10 //验证码图片
    val width = if(img.hasAttr("width")) parseInt(img.attr("width"), 200) else 0
    val height = if(img.hasAttr("height")) parseInt(img.attr("height"), 200) else 0
    if (width >= 200) score += width / 100
    if (height >= 150) score += height  / 100
    if((width > 0 && width < 200) || (height > 0 && height < 150)){ //如果图片太小，降权
      score -=50
    }
   
    
    if (img.tagName == "embed") score += 2
    if (src.contains("icon")) score -= 5
   
    
    if (src.contains("logo") || src.contains("themes")) score /= 2
    if (src.contains("thumb")) score /= 4
    if (img.attr("alt").contains("订阅")) score /= 2
    
    if(score > 0){
    	score += urlHasDate(src) * 2
    }
    val text = StringUtils.replace(img.attr("alt") + "\t" + img.attr("title"), "\u00a0", " ").trim
    if (text.length > 0 && score > 0) { //加score>0主要是为了防止有些网站为了做SEO，将关键词加到明显不是文章内容图片
      sf.searchMax(text).asScala foreach { w =>
        if (w.length >= 1) score += TextUtil.countNumWords(w)
      }
    }
    
//    println(img.absUrl("src")+":"+score)
    return score;
  }

  private def urlHasDate(url: String): Int = {
    val m = Pattern.compile("[-|/][0-9]+[-|/|.]").matcher(url)
    var start = 0
    var count = 0
    while (m.find(start)) {
      count += 1
      start = m.end - 1
    }
    count
  }

  private def parseInt(text: String, orElse: Int): Int = {
    val sb = new StringBuffer()
    for (i <- 0 until text.length) {
      val ch = text.charAt(i)
      if (ch >= '0' && ch <= '9') sb.append(ch)
      else if (sb.length() > 0) return Integer.parseInt(sb.toString())
    }
    if (sb.length() > 0) return Integer.parseInt(sb.toString())
    return orElse
  }
}