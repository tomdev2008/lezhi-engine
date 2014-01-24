package com.buzzinate.keywords.util

import org.jsoup.nodes.Element
import scala.collection.mutable.ListBuffer
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import collection.JavaConverters._
import com.buzzinate.nlp.util.TextUtil
import org.jsoup.nodes.Document
import java.util.regex.Pattern
import com.buzzinate.nlp.util.TitleExtractor
import scala.collection.mutable.HashMap
import com.abahgat.suffixtree.GeneralizedSuffixTree
import com.buzzinate.nlp.segment.AtomSplit
import org.apache.commons.lang.StringUtils
import com.buzzinate.flash.FlashThumbnail
import com.twitter.util.Future
import com.buzzinate.http.HttpScheduler

case class SnippetBlock(snippets: List[String], score: Double, isArticle: Boolean, imgs: List[Element])

case class BlockDetail(snippets: List[String], imgs: List[Element], nWords: Int, nLinkWords: Int)
class BlockDetailBuffer {
  val snippets = new ListBuffer[String]
  val imgs = new ListBuffer[Element]
  var nWords = 0
  var nLinkWords = 0
  
  def isDefined() = !snippets.isEmpty || !imgs.isEmpty
  
  def result() = BlockDetail(snippets.result, imgs.result, nWords, nLinkWords)
  
  def add(text: String, inLink: Boolean): Unit = {
    snippets += text
    val nw = TextUtil.countNumWords(text)
    if (inLink) nLinkWords += nw
    nWords += nw
  }
  
  def addImg(img: Element): Unit = {
    imgs += img
  }
  
  def clear(): Unit = {
    snippets.clear
    imgs.clear
    nWords = 0
    nLinkWords = 0
  }
}

object ExtractUtil {
  val INVALID_TAGS = Set("style", "comment", "script", "option", "iframe", "textarea", "select")
  val negativeReg = Pattern.compile("combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|outbrain|promo|related|scroll|shoutbox|sponsor|shopping|tags|tool|widget|divSidebar|function|links", Pattern.CASE_INSENSITIVE)
  val Extract_STOP_DEPTH = 50
  
  val te = new TitleExtractor
  
  def extractCanonicalUrl(doc: Document, url: String): Option[String] = {
    val canonicalLink = doc.select("link[rel=canonical]")
    if (canonicalLink.isEmpty) None else Some(canonicalLink.first.attr("href"))
  }
  
  def extractBlocks(doc: Document, title: String): List[SnippetBlock] = {
    val blocks = new ListBuffer[BlockDetail]
    val bd = new BlockDetailBuffer
    extractBlocks(doc.body, blocks, bd)
    if (bd.isDefined) blocks += bd.result
    calcScore(title, blocks.result filterNot(b => hasICP(b))) ++ List(SnippetBlock(List(extractMeta(doc, "keywords")), 1d, true, List()), SnippetBlock(List(extractMeta(doc, "description")), 0d, false, List()))
  }
  
  private def hasICP(block: BlockDetail): Boolean = {
    block.snippets.exists(snippet => snippet.toLowerCase.contains("icp备") || snippet.toLowerCase.contains("icp证"))
  }
  
  def extractThumbnail(client: HttpScheduler, blocks: List[SnippetBlock]): Future[Option[String]] = {
    val blockidxs = blocks.zipWithIndex
    val sum = Array.ofDim[Double](blocks.size + 1)
    sum(0) = 0
    blockidxs.foreach { case (block, i) =>
      val base = if (block.isArticle) 1 else 0
      sum(i+1) = sum(i) + block.score + base
    }
    
    var maxScore = 0d
    var bestImg: Element = null
    
    blockidxs.foreach { case (block, i) =>
      if (!block.imgs.isEmpty) {
        val end = math.min(i + 3, sum.length - 1)
        val start = math.max(i - 2, 0)
        val score = sum(end) - sum(start)
        block.imgs foreach { img =>
          val imgscore = (1 + scoreThumbnail(img)) * (0.5 + math.sqrt(score))
//          println(imgscore + " / " + score + "(" + start + ", " + end + ") => " + img)
          if (maxScore < imgscore) {
            maxScore = imgscore
            bestImg = img
//            println(maxScore + " ==> " + bestImg)
          }
        }
      }
    }
    
//    println("imgs: " + bestImg)

    if (bestImg == null) Future.value(None) 
    else {
      bestImg.tagName match {
        case "img" => {
          if (bestImg.hasAttr("width") && parseInt(bestImg.attr("width"), 200) < 200 || bestImg.hasAttr("height") && parseInt(bestImg.attr("height"), 150) < 150) None
          Future.value(Some(bestImg.absUrl("src")))
        }
        case "embed" => {
          FlashThumbnail.getThumbnail(client, bestImg.absUrl("src")).map { src =>
            if (src == null) None else Some(src)
          }
        }
      }
    }
  }
  
  private  def scoreThumbnail(img: Element): Double = {
    val ptag = img.parent().tagName()
    val src = img.absUrl("src").toLowerCase()
    var score = 0d
    if (ptag.equals("p")) score += 2
    if (img.hasAttr("alt")) score += 1
    if (!src.endsWith("gif")) score += 2
    if (src.contains("upload")) score += 4
    if (src.contains("attachment")) score += 4
    if (src.contains("photo")) score += 4
    if (img.hasAttr("width") && parseInt(img.attr("width"), 200) >= 200) score += parseInt(img.attr("width"), 200) * 2 / 100
    if (img.hasAttr("height") && parseInt(img.attr("height"), 150) >= 150) score += parseInt(img.attr("height"), 150) * 2 / 100
    if (img.tagName == "embed") score += 2

    score += urlHasDate(src) * 4
    
    if (src.contains("logo")) score /= 1.5
    if (src.contains("thumb")) score /= 4
    if (img.attr("alt").contains("订阅")) score /= 2
    return score;
  }
  
  private def urlHasDate(url: String): Int = {
    val m = Pattern.compile("/[0-9]+[-|/|.]").matcher(url)
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
  
  def extractMeta(doc: Document, meta: String): String = {
    val metaNodes = doc.select("meta[name=" + meta + "]")
    if (metaNodes.isEmpty) "" else metaNodes.first.attr("content")
  }
  
  def extractTitle(root: Element, rawTitle:String): String = {
    val titleCnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    titleCnt.adjustOrPut(te.extract(rawTitle).trim, 1, 1)
    titleCnt.adjustOrPut(te.extractFirst(rawTitle).trim, 1, 1)
    
    val tq = new PriorityQueue[String](2)
    extractTitle0(root, rawTitle, 1, tq)
    for (candidate <- tq.values) titleCnt.adjustOrPut(candidate.trim, 1, 1)
    
    var maxCnt = 0
    var title = rawTitle
    titleCnt.foreach { case (candidate, cnt) =>
      if (maxCnt < cnt) {
        maxCnt = cnt
        title = candidate
      }
      if (maxCnt == cnt && candidate.length > title.length) title = candidate
    }
    
    title
  }
  
  private def extractTitle0(node: Node, title: String, weight: Double, tq: PriorityQueue[String], depth: Int = 0): Unit = {
    node match {
      case textNode: TextNode => {
        val text = textNode.text.trim
        if (text.length > 0) {
          val lcs = TextUtil.findLcs(title, text)
          val nwords = TextUtil.countNumWords(lcs)
          val pos = title.indexOf(lcs)
          if (pos != -1 && nwords > 0) {
            tq.add(nwords * weight / (1 + math.log(2 + pos)), lcs)
          }
        } 
      }
      case e: Element => {
        var w = weight
        if (e.tagName.startsWith("h")) w = w * 1.2
        if (e.tagName == "a") w = w / 2
        if (e.className.contains("title")) w = w * 1.5
        if (e.tagName != "title" && !isNegativeBlock(e.className + " " + e.id) && depth < Extract_STOP_DEPTH) {
          for (n <- e.childNodes.asScala) extractTitle0(n, title, w, tq, depth + 1)
        }
      }
      case _ => {}
    }
  }
  
  def isNegativeBlock(idclazz: String): Boolean = negativeReg.matcher(idclazz).find()
  
  private def calcScore(title: String, blocks: List[BlockDetail]): List[SnippetBlock] = {
    val sf = new GeneralizedSuffixTree
    for (s <- AtomSplit.splitSnippets(title).asScala) {
      sf.put(s)
    }
    val sum = Array.ofDim[Double](blocks.size + 1)
    sum(0) = 0d
    val blockinfos = blocks.zipWithIndex map { case (block, i) =>
      val nPuncs = block.snippets.map { snippet => TextUtil.countPuncs(snippet) }.sum
      val textDensity = (block.nWords - block.nLinkWords) / (1d + block.nWords) 
      val tws = block.snippets.flatMap { snippet => sf.searchMax(snippet).asScala }.filter { w => w.length >= 2}.size
      val score = (1 + math.log(1 + nPuncs)) * (1 + textDensity) * (1 + math.log(1 + tws)) - 1
      sum(i + 1) = sum(i) + score 
      (SnippetBlock(block.snippets, score, false, block.imgs), i, score)
    } 
    
    var maxDiff = 0d
    val totalLen = blockinfos.size
    val totalScore = sum(totalLen)
    for {
      i <- 0 to totalLen
      j <- i + 1 to totalLen
    } {
      val sumij = sum(j) - sum(i)
      val lenij = j - i
      val diff = sumij / lenij - (totalScore - sumij) / (totalLen - lenij)
      if (diff > maxDiff) maxDiff = diff
    }
    
    blockinfos map { case (block, i, score) => 
//      println(i + ", m=" + maxDiff + ": " + score + " ==> " + block.snippets)
//      println("\t\t" + block.imgs)
      SnippetBlock(block.snippets, score, score * 10 > maxDiff, block.imgs) 
    }
  }
  
  private def extractBlocks(root: Node, blocks: ListBuffer[BlockDetail], bd: BlockDetailBuffer, inLink: Boolean = false, depth: Int = 0): Unit = {
    root match {
      case tn: TextNode => {
        val text = StringUtils.replace(tn.text, "\u00a0", " ").trim
        if (text.length > 0) bd.add(text, inLink)
      }
      case e: Element => {
        val isLink = inLink || (e.tagName == "a") 
        if(depth < Extract_STOP_DEPTH){
        	e.childNodes.asScala foreach { c => extractBlocks(c, blocks, bd, isLink, depth + 1) }
        }
        
        if (e.tagName == "img" || e.tagName == "embed") {
          bd.addImg(e)
        }
        if (e.isBlock) {
          if (bd.isDefined) blocks += bd.result
          bd.clear
        }
      }
      case _ => {}
    }
  }
  
  def cleanup(root: Element): Unit = {
    val cleanNodes = for {
      e <- root.getAllElements.asScala
      if INVALID_TAGS.contains(e.tagName) || e.attr("style").contains("display:none")
    } yield e
    for (cn <- cleanNodes) cn.remove
  }
}
