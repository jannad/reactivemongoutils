package com.github.jannad.reactivemongoutils

import java.util.Date

import cats.Show
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONDocument, BSONDouble, BSONInteger, BSONLong, BSONNull, BSONObjectID, BSONReader, BSONString, BSONTimestamp, BSONValue}

/**
  * @author jannad
  */
object Implicits {

	/**
	  * Converts BSONValue to human-readable string
	  */
	implicit val bsonValShow = new Show[BSONValue] {
		override def show(bsonval: BSONValue) = {
			def stringify(v: BSONValue): String = v match {
				case doc: BSONDocument => {
					val s = doc.elements
						.map(elem => s"'${elem._1}': ${stringify(elem._2)}")
						.mkString(",\n")
					s"{$s}"
				}
				case arr: BSONArray => {
					val s = arr.values.map(stringify(_)).mkString(", ")
					s"[${s}]"
				}
				case BSONLong(value) => value.toString
				case BSONInteger(value) => value.toString
				case BSONDouble(value) => value.toString
				case BSONString(value) => s"'${value.toString}'"
				case BSONDateTime(value) => new Date(value).toString
				case BSONTimestamp(value) => new Date(value).toString
				case oid: BSONObjectID => s"ObjectId('${oid.stringify}')"
				case BSONNull => "Null"
				case o => o.toString
			}

			bsonval match {
				case d: BSONDocument => s"BSONDocument(${stringify(d)})"
				case v => s"${stringify(v)}"
			}
		}
	}

	implicit val bsonDocShow: Show[BSONDocument] = new Show[BSONDocument] {
		override def show(d: BSONDocument) = bsonValShow.show(d)
	}

	implicit def extendedBSONDocument(d: BSONDocument) = ExtendedBSONDocument(Some(d))
}

/**
  * This class adds new behavior to BSONDocument class that enables navigation using the "\" operator.
  *
  * @author jannad
  */
case class ExtendedBSONDocument(doc: Option[BSONValue]) {

	/**
	  * Selects a sub-document from the wrapped document
	  *
	  * @param path
	  * @return Sub-document at the specified path
      */
	def \(path: String) = doc match {
		case Some(d: BSONDocument) => ExtendedBSONDocument(d.get(path))
		case _ => ExtendedBSONDocument(None)
	}

	/**
	  * Selects an item from the wrapped BSONArray
	  *
	  * @param index
	  * @return Element at the specified index wrapped in ExtendedBSONDocument, if the referenced document is an instance of BSONArray.
	  *         ExtendedBSONDocument(None) otherwise.
      */
	def \(index: Int) = doc match {
		case Some(arr: BSONArray) => ExtendedBSONDocument(arr.get(index))
		case _ => ExtendedBSONDocument(None)
	}

	/**
	  * Tries to produce an instance of `T` from the wrapped BSON value
	  *
      * @return Some(t of T) if the wrapped value is compatible with `T`. None otherwise.
      */
	def as[T](implicit reader: BSONReader[_ <: BSONValue, T]): Option[T] = doc match {
		case None => Option.empty[T]
		case Some(v: BSONValue) => {
			reader match {
				case r: BSONReader[BSONValue, T]@unchecked => r.readOpt(v)
				case _ => None
			}
		}
	}

	override def toString() = {
		import  com.github.jannad.reactivemongoutils.Implicits.bsonValShow
		import cats.syntax.show._
		s"ExtendedBSONDocument(${doc.map(d => d.show).getOrElse("None")})"
	}
}
