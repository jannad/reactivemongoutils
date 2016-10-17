package com.github.jannad.reactivemongoutils

import reactivemongo.bson.{BSONArray, BSONDocument, BSONObjectID}

/**
  * Examples of using ExtendedBSONDocument and BSONPathInterpolation
  */
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
