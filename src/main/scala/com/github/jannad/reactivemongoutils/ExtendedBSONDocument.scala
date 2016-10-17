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
		override def show(v: BSONValue) = {
			def f(v: BSONValue): String = v match {
				case d: BSONDocument => {
					val s = d.elements
						.map(elem => s"'${elem._1}': ${f(elem._2)}")
						.mkString(",\n")
					s"{$s}"
				}
				case a: BSONArray => {
					val s = a.values.map(f(_)).mkString(", ")
					s"[${s}]"
				}
				case o: BSONLong => o.value.toString
				case o: BSONInteger => o.value.toString
				case o: BSONDouble => o.value.toString
				case o: BSONString => s"'${o.value.toString}'"
				case o: BSONDateTime => new Date(o.value).toString
				case o: BSONTimestamp => new Date(o.value).toString
				case o: BSONObjectID => s"ObjectId('${o.stringify}')"
				case BSONNull => "Null"
				case o => o.toString
			}

			v match {
				case d: BSONDocument => s"BSONDocument(${f(d)})"
				case v => s"${f(v)}"
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
	def \(path: String) = doc match {
		case Some(d: BSONDocument) => ExtendedBSONDocument(d.get(path))
		case _ => ExtendedBSONDocument(None)
	}

	def \(index: Int) = doc match {
		case Some(arr: BSONArray) => ExtendedBSONDocument(arr.get(index))
		case _ => ExtendedBSONDocument(None)
	}

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

object Demo {
	import Implicits._

	val doc = BSONDocument(
		"subdoc1" -> BSONDocument("intField" -> 123),
		"id" -> BSONObjectID("507f1f77bcf86cd799439011"),
		"subdoc2" -> BSONDocument("subdoc2_1" -> BSONDocument("strField" -> "xxxx"), "arr" -> BSONArray(1, 2, 3, 4, 5)
		)
	)

	def main(args: Array[String]) {
		println((doc \ "subdoc2" \ "subdoc2_1" \ "strField").as[String])
		println((doc \ "subdoc1" \ "intField").as[Int])
		println((doc \ "subdoc2" \ "arr" \ 1).as[Int])
		println((doc \ "abc" \ "def" \ 1).as[Double])

		import cats.syntax.show._
		println(doc.show)

		import BSONPathInterpolation._
		val index = 3
		val arrField = "arr"
		println(path"subdoc2.subdoc2_1.strField"(doc).as[String])
		println(path"subdoc2.$arrField.$index"(doc).as[Int])

	}
}
