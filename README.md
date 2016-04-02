# reactivemongoutils
Little helpers for working with reactive mongo and BSON documents

##Usage

### Example
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
println(doc \ "subdoc2" \ "arr" \ 1)

import cats.syntax.show._
import com.github.jannad.reactivemongoutils.Implicits.bsonDocShow
println(doc.show)
```
### Output
```
Some(xxxx)
Some(123)
Some(2)
ExtendedBSONDocument(BSONDocument(2))
BSONDocument({'subdoc1': {'intField': 123}, 'id': ObjectId('507f1f77bcf86cd799439011'), 'subdoc2': {'subdoc2.1': {'strField': 'xxxx'}, 'arr': [1, 2, 3, 4, 5]}})
```
