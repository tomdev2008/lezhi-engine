package com.shorrockin.cascal.model

import java.nio.ByteBuffer

/**
 * a type of column container which holds standard columns.
 *
 * @author Chris Shorrock
 */
trait StandardColumnContainer[ColumnType, SliceType, ValueType] extends ColumnContainer[ColumnType, SliceType] {
  def \(name:ByteBuffer):ColumnType
  def \(name:ByteBuffer, value:ValueType):ColumnType
}
