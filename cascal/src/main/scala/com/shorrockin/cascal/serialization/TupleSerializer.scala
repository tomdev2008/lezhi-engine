package com.shorrockin.cascal.serialization

import java.nio.ByteBuffer
import java.util.{Date, UUID}
import me.prettyprint.cassandra.serializers._

object TupleSerializer {

  def extractType[T](bytes: ByteBuffer, mf: Manifest[T]): T = {
    val length = (bytes.get() & 0xFF) << 8 | (bytes.get() & 0xFF)
    val typeBuffer = bytes.duplicate
    typeBuffer.limit(typeBuffer.position + length)
    
    bytes.position(typeBuffer.position + length + 1)
    
    val ser = Serializer.Default(mf.erasure)
    ser.fromByteBuffer(typeBuffer).asInstanceOf[T]
  }
  
  def byteBuffer[T](value: T)(implicit mf: Manifest[T]): ByteBuffer = {
    value match {
      case x: String if mf.erasure == classOf[String] => StringSerializer.get.toByteBuffer(x)
      case x: UUID if mf.erasure == classOf[UUID] => UUIDSerializer.get.toByteBuffer(x)
      case x: Int if mf.erasure == classOf[Int] => IntegerSerializer.get.toByteBuffer(x)
      case x: Long if mf.erasure == classOf[Long] => LongSerializer.get.toByteBuffer(x)
      case x: Boolean if mf.erasure == classOf[Boolean] => BooleanSerializer.get.toByteBuffer(x)
      case x: Float if mf.erasure == classOf[Float] => FloatSerializer.get.toByteBuffer(x)
      case x: Double if mf.erasure == classOf[Double] => DoubleSerializer.get.toByteBuffer(x)
      case x: Date if mf.erasure == classOf[Date] => DateSerializer.get.toByteBuffer(x)
      case None => ByteBuffer.allocate(0)
    }
  }
}

class CompositeBuffer(val buffers: ByteBuffer*) {
  
  val lengthBytesSize = 2
  val endOfComponentSize = 1
  val compositeOverheadSize = lengthBytesSize + endOfComponentSize
  
  def buffer(): ByteBuffer = {
    val buffersSize = buffers.foldLeft(0){(sum, buffer) => sum + buffer.remaining}
    val requiredSize = buffersSize + buffers.size * compositeOverheadSize
    val buffer = ByteBuffer.allocate(requiredSize)
    
    buffers foreach {buff =>
      buffer.putShort(buff.remaining.asInstanceOf[Short]).put(buff).put(0.toByte)
    }
    buffer.rewind
    buffer
  }
}

object Tuple2Serializer  {
  import TupleSerializer._
  
  def toByteBuffer[T1: Manifest, T2: Manifest](tuple: Tuple2[T1, T2]): ByteBuffer = {
    val buffer = new CompositeBuffer(byteBuffer(tuple._1), byteBuffer(tuple._2))
    buffer.buffer
  }
  
  def fromByteBuffer[T1, T2](bytes:ByteBuffer, mf1: Manifest[T1], mf2: Manifest[T2]): Tuple2[T1, T2] = {
    (extractType(bytes, mf1), extractType(bytes, mf2))
  }
}

object Tuple3Serializer  {
  import TupleSerializer._
  
  def toByteBuffer[T1: Manifest, T2: Manifest, T3: Manifest](tuple: Tuple3[T1, T2, T3]): ByteBuffer = {
    val buffer = new CompositeBuffer(byteBuffer(tuple._1), byteBuffer(tuple._2), byteBuffer(tuple._3))
    buffer.buffer
  }
  
  def fromByteBuffer[T1, T2, T3](bytes:ByteBuffer, mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3]): Tuple3[T1, T2, T3] = {
    (extractType(bytes, mf1), extractType(bytes, mf2), extractType(bytes, mf3))
  }
}

object Tuple4Serializer  {
  import TupleSerializer._
  
  def toByteBuffer[T1: Manifest, T2: Manifest, T3: Manifest, T4: Manifest](tuple: Tuple4[T1, T2, T3, T4]): ByteBuffer = {
    val buffer = new CompositeBuffer(byteBuffer(tuple._1), byteBuffer(tuple._2), byteBuffer(tuple._3), byteBuffer(tuple._4))
    buffer.buffer
  }
  
  def fromByteBuffer[T1, T2, T3, T4](bytes:ByteBuffer, mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3], mf4: Manifest[T4]): Tuple4[T1, T2, T3, T4] = {
    (extractType(bytes, mf1), extractType(bytes, mf2), extractType(bytes, mf3), extractType(bytes, mf4))
  }
}

object Tuple5Serializer  {
  import TupleSerializer._
  
  def toByteBuffer[T1: Manifest, T2: Manifest, T3: Manifest, T4: Manifest, T5: Manifest](tuple: Tuple5[T1, T2, T3, T4, T5]): ByteBuffer = {
    val buffer = new CompositeBuffer(byteBuffer(tuple._1), byteBuffer(tuple._2), byteBuffer(tuple._3), byteBuffer(tuple._4), byteBuffer(tuple._5))
    buffer.buffer
  }
  
  def fromByteBuffer[T1, T2, T3, T4, T5](bytes:ByteBuffer, mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3], mf4: Manifest[T4], mf5: Manifest[T5]): Tuple5[T1, T2, T3, T4, T5] = {
    (extractType(bytes, mf1), extractType(bytes, mf2), extractType(bytes, mf3), extractType(bytes, mf4), extractType(bytes, mf5))
  }
}