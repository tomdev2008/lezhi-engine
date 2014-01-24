package com.buzzinate.keywords.content

import collection.JavaConverters._
import com.buzzinate.keywords.util.{PriorityQueue, SnippetBlock, HashMapUtil}
import scala.collection.mutable.HashMap
import com.buzzinate.nlp.segment.AtomSplit
import com.buzzinate.nlp.segment.Atom.AtomType
import scala.collection.mutable.ListBuffer
import com.buzzinate.nlp.ngram.NgramBuilder
import com.buzzinate.nlp.dlg.AtomFreq
import com.buzzinate.nlp.dlg.BestDlg.Dict
import com.buzzinate.nlp.segment.Atom
import com.buzzinate.nlp.dlg.BestDlg
import org.ansj.splitWord.Segment
import com.buzzinate.nlp.graph.GraphRank
import java.util.ArrayList
import org.arabidopsis.ahocorasick.WordFreqTree
import com.buzzinate.nlp.util.DictUtil

class VDict(ngram2dlg: HashMap[String, Double]) extends Dict {
  def dlg(ngram: java.util.List[String]): Double = ngram2dlg.getOrElse(Atom.join(ngram), -1d)
}

object DlgExtractor {
  def extract(title: String, rawTitle: String, blocks: List[SnippetBlock], maxLen: Int): List[(String, Int)] = {
    val rawsnippets = blocks flatMap { b => b.snippets }
    val atomCnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    
    rawsnippets foreach { snippet =>
      AtomSplit.split(snippet).asScala foreach { atom =>
        if (atom.atomType != AtomType.AT_PUNC) atomCnt.adjustOrPut(atom.token, 1, 1)
      }
    }
    val atomFreq = new AtomFreq(atomCnt.map { case (word, freq) => word -> new java.lang.Integer(freq)}.asJava)
    
    val snippets = new ListBuffer[java.util.List[String]]
    blocks filter (_.isArticle) foreach { block =>
      block.snippets foreach { snippet =>
        AtomSplit.splitSentences(snippet).asScala foreach { words =>
          val atoms = AtomSplit.split(words).asScala.map(atom => atom.token)
          snippets += atoms.asJava
        }
      }
    } 
    val ngramFreq = NgramBuilder.build(snippets.result.asJava, maxLen).asScala
   
    val ngram2dlg = new HashMap[String, Double]
    ngramFreq filter(x => Character.isLetter(x._1.charAt(0))) foreach { case (ngram, freq) =>
      val dlg = atomFreq.dlg(AtomSplit.split0(ngram), freq)
//      println(ngram + " => freq=" + freq + ", dlg=" + dlg)
      if (dlg > 0) {
        ngram2dlg += ngram -> dlg
      }
    }
    
    val vd = new VDict(ngram2dlg)
    
    val wordTree = new WordFreqTree
    blocks filter (_.isArticle) foreach { block =>
      block.snippets foreach { snippet =>
        AtomSplit.splitSnippets(snippet).asScala foreach { words =>
          val r = BestDlg.splitDlg(vd, words, maxLen).asScala filter { w =>
            val length = AtomSplit.atomLength(w)
            length > 1 && length < maxLen && Character.isLetter(w.charAt(0)) && !DictUtil.isStop(w) 
          } foreach { w =>
            wordTree.add(w)
          }
        }
      }
    }
    wordTree.build
    
    val wordFreq = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    
    val titleWords = split(title, wordTree)
    val brandWords = split(rawTitle, wordTree)
    brandWords.removeAll(titleWords)
    titleWords.asScala foreach { word => wordFreq.adjustOrPut(word, 1, 1) }
//    println(title + " => " + titleWords)
    
    var bestWords = titleWords
    var bestScore = 1d
    val g = new GraphRank
    blocks filter (_.isArticle) foreach { case block =>
      val all = new java.util.ArrayList[String]
      block.snippets foreach { snippet =>
        val words = split(snippet, wordTree)
        all.addAll(words)
        words.asScala.foreach { word => wordFreq.adjustOrPut(word, 1, 1) }
        if (words.size > 0) g.addRow(words)
      }
      if (bestScore < block.score) {
        bestWords = all
        bestScore = block.score
      }
    }
    
    val lwq2 = new PriorityQueue[(String, Int)](15)
    val lwq1 = new PriorityQueue[(String, Int)](8)
    val swq2 = new PriorityQueue[(String, Int)](10)
    val swq1 = new PriorityQueue[(String, Int)](5)
    
    bestWords.removeAll(brandWords)
    val brandScore = g.rank(brandWords, bestWords)
    g.rank(bestWords, brandWords).asScala foreach { case (word, score) =>
      val freq = wordFreq(word)
      val idf = DictUtil.splitIdf(word)
      var bs = brandScore.get(word)
      if (bs == null) bs = 0d
      val al = AtomSplit.atomLength(word)
      val adjustScore = score * freq * idf / (1 + bs)
//      println(word + " => adjustscore=" + adjustScore + ", score=" + score + ", brandscore=" + bs + ", idf=" + idf)
      if (al > 2) {
        if (freq > 1 && score > bs * 2) lwq2.add(adjustScore, word -> freq)
        if (score > bs) lwq1.add(adjustScore, word -> freq)
      } else {
        if (freq > 1 && score > bs * 2) swq2.add(adjustScore, word -> freq)
        if (score > bs) swq1.add(adjustScore, word -> freq)
      }
    }
    
    merge(lwq2.values, 3, lwq1.values) ++ merge(swq2.values, 5, swq1.values)
  }
  
  private def merge[T](list1: Iterable[T], min: Int, list2: Iterable[T]): List[T] = {
    if (list1.size >= min) list1.toList else list2.toList
  }
  
  def split(snippet: String, wordTree: WordFreqTree): java.util.List[String] = {
     val words = new ArrayList[String]
     words.addAll(wordTree.search(snippet))
     val wordset = words.asScala.toSet
     Segment.split(snippet).asScala foreach { term =>
       if (term.getName.length > 1 && !DictUtil.isUseless(term) && !DictUtil.isStop(term.getName)) {
         if (!wordset.contains(term.getName)) words.add(term.getName)
       }
     }
     words
  }
}