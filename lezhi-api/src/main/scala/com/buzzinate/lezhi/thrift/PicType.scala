/**
 * generated by Scrooge 3.0.9
 */
package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.ThriftEnum


@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"), date = "2013-07-08T14:37:54.086+0800")
case object PicType {
  
  case object Text extends PicType {
    val value = 0
    val name = "Text"
  }
  
  case object Inpage extends PicType {
    val value = 1
    val name = "Inpage"
  }
  
  case object Insite extends PicType {
    val value = 2
    val name = "Insite"
  }
  
  case object Provided extends PicType {
    val value = 3
    val name = "Provided"
  }

  /**
   * Find the enum by its integer value, as defined in the Thrift IDL.
   * @throws NoSuchElementException if the value is not found.
   */
  def apply(value: Int): PicType = {
    value match {
      case 0 => Text
      case 1 => Inpage
      case 2 => Insite
      case 3 => Provided
      case _ => throw new NoSuchElementException(value.toString)
    }
  }

  /**
   * Find the enum by its integer value, as defined in the Thrift IDL.
   * Returns None if the value is not found
   */
  def get(value: Int): Option[PicType] = {
    value match {
      case 0 => scala.Some(Text)
      case 1 => scala.Some(Inpage)
      case 2 => scala.Some(Insite)
      case 3 => scala.Some(Provided)
      case _ => scala.None
    }
  }

  def valueOf(name: String): Option[PicType] = {
    name.toLowerCase match {
      case "text" => scala.Some(PicType.Text)
      case "inpage" => scala.Some(PicType.Inpage)
      case "insite" => scala.Some(PicType.Insite)
      case "provided" => scala.Some(PicType.Provided)
      case _ => scala.None
    }
  }
}



@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"), date = "2013-07-08T14:37:54.086+0800")
sealed trait PicType extends ThriftEnum with Serializable