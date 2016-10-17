package com.github.jannad.reactivemongoutils

import reactivemongo.bson.BSONValue

/**
  * @author jannad
  *
  * Custom string interpolation for BSON document paths.
  * See [[http://docs.scala-lang.org/overviews/core/string-interpolation.html String Interpolation]]
  *
  * Usage: path"fieldA.fieldB.${fieldC}.array1.10" produces a function that can be applied to BSONValue
  */
object BSONPathInterpolation {
	sealed trait BSONPathPart
	sealed case class BSONFieldName(name: String) extends BSONPathPart
	sealed case class BSONArrayIndex(idx: Int) extends BSONPathPart

	implicit class BSONPathHelper(val sc: StringContext) extends AnyVal {

		def path(args: Any*): BSONValue => ExtendedBSONDocument = {
			// Interleaves string portions of the path (stored in parts) with expression values (i.e. the "${...}" parts) in args
			def interleave(args: List[Any], parts: List[String]): List[Any] = {
				(args, parts) match {
					case (Nil, p::Nil) => p::Nil
					case (a1::xa, p1::xp) => p1::a1::interleave(xa, xp)
				}
			}

			// Expanded path (expressions replaced with values)
			val pathStr = interleave(args.toList, sc.parts.toList).map(_.toString)

			val Num = "([0-9])+".r
			val pathParts: Array[BSONPathPart] = pathStr.mkString.split("\\.").map(_ match {
				case Num(n) => BSONArrayIndex(n.toInt)
				case p => BSONFieldName(p)
			})

			def runPath(path: Array[BSONPathPart])(b: BSONValue) = {
				val root = ExtendedBSONDocument(Some(b))
				path.foldLeft(root)((doc, pathPart) => {
					pathPart match {
						case BSONFieldName(s) => doc \ s
						case BSONArrayIndex(n) => doc \ n
					}
				})
			}

			runPath(pathParts)
		}
	}
}
