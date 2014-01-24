package com.buzzinate.dm.keywords

import com.buzzinate.nlp.util.TitleExtractor

object Global {
   val te = new TitleExtractor
   
   def extractTitle(title: String) = te.extract(title)
}