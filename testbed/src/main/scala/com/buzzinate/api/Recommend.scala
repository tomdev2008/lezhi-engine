package com.buzzinate.api

case class TopItem(itemId: Int, score: Double)

trait Recommend {
  def recommend(u: Int, topN: Int): List[TopItem]
}

trait RecommendBuilder {
  def train(trainset: List[Rating]): Map[String, Recommend]
}