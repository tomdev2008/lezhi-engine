package com.buzzinate.algos

import com.buzzinate.api.Recommend
import scala.collection.immutable.List
import com.buzzinate.api.Rating
import com.buzzinate.api.TopItem
import com.buzzinate.api.RecommendBuilder

class NullRecommend extends Recommend with RecommendBuilder {
  def train(trainset: List[Rating]): Map[String, Recommend] = Map("null" -> this)
  def recommend(u: Int, topN: Int): List[TopItem] = Nil
}