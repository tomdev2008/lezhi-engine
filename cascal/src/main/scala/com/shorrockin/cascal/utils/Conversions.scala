package com.shorrockin.cascal.utils

import java.nio.charset.Charset
import com.shorrockin.cascal.model.{Column, Keyspace}
import java.util.{Date, UUID => JavaUUID}
import com.shorrockin.cascal.serialization._
import java.nio.ByteBuffer
import me.prettyprint.cassandra.serializers._

/**
 * some implicits to assist with common conversions
 */
object Conversions {
  val utf8 = Charset.forName("UTF-8")

  implicit def keyspace(str:String) = new Keyspace(str)

  implicit def byteBuffer(date:Date):ByteBuffer = DateSerializer.get.toByteBuffer(date)
  implicit def date(bytes:ByteBuffer):Date = DateSerializer.get.fromByteBuffer(bytes)

  implicit def byteBuffer(b:Boolean):ByteBuffer = BooleanSerializer.get.toByteBuffer(b)
  implicit def boolean(bytes:ByteBuffer):Boolean = BooleanSerializer.get.fromByteBuffer(bytes)

  implicit def byteBuffer(b:Float):ByteBuffer = FloatSerializer.get.toByteBuffer(b)
  implicit def float(bytes:ByteBuffer):Float = FloatSerializer.get.fromByteBuffer(bytes)

  implicit def byteBuffer(b:Double):ByteBuffer = DoubleSerializer.get.toByteBuffer(b)
  implicit def double(bytes:ByteBuffer):Double = DoubleSerializer.get.fromByteBuffer(bytes)
 

  implicit def byteBuffer(l:Long):ByteBuffer = LongSerializer.get.toByteBuffer(l)
  implicit def long(bytes:ByteBuffer):Long = LongSerializer.get.fromByteBuffer(bytes)

  implicit def byteBuffer(i:Int):ByteBuffer = IntegerSerializer.get.toByteBuffer(i)
  implicit def int(bytes:ByteBuffer):Int = IntegerSerializer.get.fromByteBuffer(bytes)

  implicit def byteBuffer(str:String):ByteBuffer = StringSerializer.get.toByteBuffer(str)
  implicit def string(bytes:ByteBuffer):String = StringSerializer.get.fromByteBuffer(bytes)
  
  implicit def byteBuffer(uuid: JavaUUID):ByteBuffer = UUIDSerializer.get.toByteBuffer(uuid)
  implicit def uuid(bytes:ByteBuffer):JavaUUID = UUIDSerializer.get.fromByteBuffer(bytes)
  implicit def string(uuid: JavaUUID): String = uuid.toString

  implicit def string(col:Column[_]):String = {
    "%s -> %s (time: %s)".format(Conversions.string(col.name), Conversions.string(col.value), col.time)
  }

  implicit def toSeqBytes(values:Seq[String]) = values.map { (s) => Conversions.byteBuffer(s) }
  implicit def toJavaList[T](l: Seq[T]):java.util.List[T] = l.foldLeft(new java.util.ArrayList[T](l.size)){(al, e) => al.add(e); al}
  
  implicit def byteBuffer[T1: Manifest, T2: Manifest](tuple: Tuple2[T1, T2]):ByteBuffer = Tuple2Serializer.toByteBuffer(tuple)
  implicit def tuple[T1, T2](bytes:ByteBuffer)(implicit mf1: Manifest[T1], mf2: Manifest[T2]):Tuple2[T1, T2] = Tuple2Serializer.fromByteBuffer[T1, T2](bytes, mf1, mf2)
  
  implicit def byteBuffer[T1: Manifest, T2: Manifest, T3: Manifest](tuple: Tuple3[T1, T2, T3]):ByteBuffer = Tuple3Serializer.toByteBuffer(tuple)
  implicit def tuple[T1, T2, T3](bytes:ByteBuffer)(implicit mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3]):Tuple3[T1, T2, T3] = Tuple3Serializer.fromByteBuffer[T1, T2, T3](bytes, mf1, mf2, mf3)
  
  implicit def byteBuffer[T1: Manifest, T2: Manifest, T3: Manifest, T4: Manifest](tuple: Tuple4[T1, T2, T3, T4]):ByteBuffer = Tuple4Serializer.toByteBuffer(tuple)
  implicit def tuple[T1, T2, T3, T4](bytes:ByteBuffer)(implicit mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3], mf4: Manifest[T4]):Tuple4[T1, T2, T3, T4] = Tuple4Serializer.fromByteBuffer[T1, T2, T3, T4](bytes, mf1, mf2, mf3, mf4)
  
  implicit def byteBuffer[T1: Manifest, T2: Manifest, T3: Manifest, T4: Manifest, T5: Manifest](tuple: Tuple5[T1, T2, T3, T4, T5]):ByteBuffer = Tuple5Serializer.toByteBuffer(tuple)
  implicit def tuple[T1, T2, T3, T4, T5](bytes:ByteBuffer)(implicit mf1: Manifest[T1], mf2: Manifest[T2], mf3: Manifest[T3], mf4: Manifest[T4], mf5: Manifest[T5]):Tuple5[T1, T2, T3, T4, T5] = Tuple5Serializer.fromByteBuffer[T1, T2, T3, T4, T5](bytes, mf1, mf2, mf3, mf4, mf5)
}
