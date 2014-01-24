package com.buzzinate.lezhi.tools

import com.buzzinate.lezhi.crawl.Vars
import collection.JavaConverters._
import org.buzzinate.lezhi.api.Client

object UpdateTemplate {

  def main(args: Array[String]): Unit = {
    val name = "content_tpl"
    val tpl = 
"""{
    "template" : "content_*",
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
        "doc" : {
          "properties" : {
            "url" : {"type" : "string", "index" : "not_analyzed", "omit_norms": "true", "store": "yes"},
	    "title" : {"type" : "string", "index" : "no", "store": "yes"},
	    "signature" : {"type" : "string", "index" : "not_analyzed", "omit_norms": "true", "store": "yes"},
	    "thumbnail" : {"type" : "string", "index" : "no", "store": "yes"},
            "keyword" : {"type" : "string", "index_analyzer" : "lezhi_keyword", "index_options": "positions", "omit_norms": "true", "store": "no"},
            "lastModified" : {"type" : "long", "store": "yes"}
	  }
        }
    }
}"""
  
    val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")
    val vars = new Vars(prop.asInstanceOf[java.util.Map[String, String]].asScala.toMap)
    val client = new Client(vars.elasticsearchHosts.asJava)
    println(vars.elasticsearchHosts)
//    println(client.numDocs("e.gmw.cn"))
    println("update template: " + client.updateTemplate(name, tpl))
    client.close
  }
}