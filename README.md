# reactivemongoutils
Little helpers for working with [ReactiveMongo](http://reactivemongo.org/) and BSON documents, written in Scala.

#Usage

##1) "\" syntax for traversing BSONValue structures
Use implicit conversion to ExtendedBSONDocument that enables a syntax similar to Play JSON JsValue with an extension for accessing BSONArray elements. You can either use string path elements to traverse fields, or integers to access elements of BSONArray-s.

### Example Code
```Scala
val doc = BSONDocument(
	"subdoc1" -> BSONDocument("intField" -> 123),
	"id" -> BSONObjectID("507f1f77bcf86cd799439011"),
	"subdoc2" -> BSONDocument(
		"subdoc2_1" -> BSONDocument("strField" -> "string"),
		"arr" -> BSONArray(1, 2, 3, 4, 5)
	)
)
	
import Implicits._
println((doc \ "subdoc2" \ "subdoc2.1" \ "strField").as[String])
println((doc \ "subdoc1" \ "intField").as[Int])
println((doc \ "subdoc2" \ "arr" \ 1).as[Int])
println((doc \ "abc" \ "def" \ 1).as[Double])
println(doc \ "subdoc2" \ "arr")

// Compare this code without ExtendedBSONDocument and the slash syntax:
doc.getAs[BSONDocument]("subdoc2")
	.fold(Option.empty[BSONDocument])(_.getAs[BSONDocument]("subdoc2.1"))
	.fold(Option.empty[String])(_.getAs[String]("strField"))
// ...with ExtendedBSONDocument:
(doc \ "subdoc2" \ "subdoc2_1" \ "strField").as[String]
```
### Output
```
Some(string)
Some(123)
Some(2)
None
ExtendedBSONDocument([1, 2, 3, 4, 5])
```

##2) Path Interpolation
Custom String interpolation for defining applicable paths on BSON documents. ```path"$pathString"``` produces a function from BSONValue to ExtendedBSONDocument.

### Example Code
```Scala
import BSONPathInterpolation._

val index = 3
val arrField = "arr"
println(path"subdoc2.subdoc2_1.strField"(doc).as[String])
println(path"subdoc2.$arrField.$index"(doc).as[Int])
```

### Output
```
Some(xxxx)
Some(4)
```

##3) Show[BSONValue] and Show[BSONDocument] typeclasses for [Cats](http://typelevel.org/cats/typeclasses.html)
These give you nice stringification of BSONDocument-s, rather than the standard "BSONDocument(non-empty)" produced by BSONDocument.toString

### Example Code
```Scala
import cats.syntax.show._
import com.github.jannad.reactivemongoutils.Implicits.bsonDocShow
println(doc.show)
```

### Output
```
BSONDocument({'subdoc1': {'intField': 123},
'id': ObjectId('507f1f77bcf86cd799439011'),
'subdoc2': {'subdoc2.1': {'strField': 'xxxx'},
'arr': [1, 2, 3, 4, 5]}})
```
