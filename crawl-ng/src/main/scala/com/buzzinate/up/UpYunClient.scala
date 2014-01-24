package com.buzzinate.up

import scala.collection.mutable.HashMap
import java.io.File
import scala.io.Source
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import com.twitter.util.Future
import com.twitter.util.Promise
import collection.JavaConverters._
import com.buzzinate.http.Http
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity
import com.buzzinate.http.DefaultHttpScheduler
import java.util.concurrent.Executors
import com.buzzinate.http.HttpResponse

case class UploadedFile(url: String, info: Map[String, String])

class UpYunClient(bucketname: String, username: String, rawPassword: String) {
  val password = UpYunUtil.md5(rawPassword)
  val api_domain = "v0.api.upyun.com"
    
  def upload(dir: String, filename: String, data: Array[Byte], auto: Boolean): Future[UploadedFile] = {
    val headers = new HashMap[String, String]
    val date = UpYunUtil.getGMTDate
    headers += "Date" -> date
    headers += "Content-Md5" -> UpYunUtil.md5(data)
    if(auto) headers += "mkdir" -> "true"
    val filepath = "/" + StringUtils.replaceChars(dir, '.', '_') + filename
    val sign = UpYunUtil.sign("PUT", "/" + bucketname + filepath, date, data.length, username, password)
//    println(sign)
    headers += "Authorization" -> sign
    
    val req = new PutMethod("http://" + api_domain + "/" + bucketname + filepath)
    val entity = new ByteArrayRequestEntity(data)
    req.setRequestEntity(entity)
    headers.foreach { case (name, value) =>
      req.addRequestHeader(name, value)
    }
    
    UpYunClient.client.submit(req).map { resp =>
//      println(resp.statusCode + " => " + HttpResponse.toHtml("", resp).html)
      val headers = Map() ++ resp.headers.map(h => h.getName -> h.getValue)
      UploadedFile("http://" + bucketname + ".b0.upaiyun.com" + filepath, headers)
    }
  }
}

object UpYunClient {
  val client = new DefaultHttpScheduler(Http.build(10, 10), Executors.newFixedThreadPool(16))
}

object TestUpyun {
  def main(args: Array[String]): Unit = {
    val upyun = new UpYunClient("lezhi", "buzzinate", "buzzinate")
    val file = new File("D:/Temp/5281635_180814354171_2.jpg")
    val bs = FileUtils.readFileToByteArray(file)
    println(upyun.upload("test1.com", "/newtest44.jpg", bs, true).get)
    println(upyun.upload("test1.com", "/newtest2.jpg", bs, true).get)
    
    Thread.sleep(1000 * 300)
  }
}