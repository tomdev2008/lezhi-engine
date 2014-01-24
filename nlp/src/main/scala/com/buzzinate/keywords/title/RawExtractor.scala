package com.buzzinate.keywords.title

case class WordCandidate(word: String, freq: Int)

trait RawExtractor {
  def extract(title: String, snippets: List[String]): List[WordCandidate] = {
    extract(List(), title, snippets)
  }
  
  def extract(rawWordCandidates: List[WordCandidate], title: String, snippets: List[String]): List[WordCandidate]
}