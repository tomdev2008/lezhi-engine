package com.buzzinate.lezhi.test

import java.net.URLEncoder
import java.net.URLDecoder

object Test {

  def main(args: Array[String]): Unit = {
    println(URLDecoder.decode("http://changchun.51chudui.com/Class/Class.asp?DB18=%28100000-200000%29%u5143&ID=438", "UTF-8"))
    println(URLEncoder.encode("http://changchun.51chudui.com/Class/Class.asp?DB18=%28100000-200000%29%u5143&ID=438", "UTF-16"))
    println("http://changchun.51chudui.com/Class/Class.asp?DB18=%28100000-200000%29%u5143&ID=438".substring(70))
  }

}