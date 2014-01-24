package com.buzzinate.lezhi.test

import com.buzzinate.model.DelimitedKeywordText
import com.buzzinate.nlp.util.DictUtil

object ToIdftext {
  def main(args: Array[String]): Unit = {
    val idftext = DelimitedKeywordText.toIdfText("发言人|1,0 生活费|3,2 重要|2,0 经纪人|1,0 爸爸|6,0 大小|4,3 大小s|4,3 10|5,2 爸病|3,2 取消|5,0 姐妹|5,0 爸病重大小|2,2 万生活费|2,2 10万|3,2 小s|10,3 扮演|2,0 生活|5,2 s爸|10,2 父亲|2,2") { word => DictUtil.splitIdf(word)}
    println(idftext)
  }
}