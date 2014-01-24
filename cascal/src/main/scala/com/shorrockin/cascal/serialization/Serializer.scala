package com.shorrockin.cascal.serialization

import me.prettyprint.cassandra.serializers._
import me.prettyprint.cassandra.serializers.StringSerializer
import java.util.UUID
import java.util.Date

object Serializer {

  /**
   * defines a map of all the default serializers
   */
  val Default = Map[Class[_], AbstractSerializer[_]](
    (classOf[String]  -> StringSerializer.get),
    (classOf[UUID]    -> UUIDSerializer.get),
    (classOf[Int]     -> IntegerSerializer.get),
    (classOf[Long]    -> LongSerializer.get),
    (classOf[Boolean] -> BooleanSerializer.get),
    (classOf[Float]   -> FloatSerializer.get),
    (classOf[Double]  -> DoubleSerializer.get),
    (classOf[Date]    -> DateSerializer.get)
  )
}