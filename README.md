# reactivemongoutils
Little helpers for working with reactive mongo and BSON documents

#Usage

##1) "\" syntax for traversing BSONValue structures
Use implicit conversion to ExtendedBSONDocument that enables a syntax similar to Play JSON JsValue with an extension for accessing BSONArray elements. You can either use string paths elements to traverse fields, or integers to access elements of BSONArray-s.

### Example Code
```Scala
val doc = BSONDocument(
	"subdoc1" -> BSONDocument("intField" -> 123),
	"id" -> BSONObjectID("507f1f77bcf86cd799439011"),
	"subdoc2" -> BSONDocument(
		"subdoc2.1" -> BSONDocument("strField" -> "string"),
		"arr" -> BSONArray(1, 2, 3, 4, 5)
	)
)
	
import Implicits._
println((doc \ "subdoc2" \ "subdoc2.1" \ "strField").as[String])
println((doc \ "subdoc1" \ "intField").as[Int])
println((doc \ "subdoc2" \ "arr" \ 1).as[Int])
println(doc \ "subdoc2" \ "arr")
```
### Output
```
Some(string)
Some(123)
Some(2)
ExtendedBSONDocument([1, 2, 3, 4, 5])
```

##2) Show[BSONValue] and Show[BSONDocument] typeclasses for [Cats](http://typelevel.org/cats/typeclasses.html)
These give you nice stringification of BSONDocument-s, rather than the standard "BSONDocument(<non-empty>)" produced by BSONDocument.toString

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
