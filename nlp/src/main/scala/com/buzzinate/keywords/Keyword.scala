package com.buzzinate.keywords

case class Keyword(word: String, field: Int, freq: Int, score: Double)

object Keyword {
  def apply(word: String, field: Int, freq: Int): Keyword = {
    Keyword(word, field, freq, 0)
  }
}