package com.github.jannad.reactivemongoutils

import org.scalatest._
import reactivemongo.bson.{BSONArray, BSONDocument, BSONInteger, BSONObjectID}

/**
  * @author jannad
  */
class ExtendedBSONDocumentTest extends FlatSpec with Matchers {

    import Implicits._

    val subdoc2 = BSONDocument(
        "subdoc2_1" -> BSONDocument("strField" -> "subdoc2_1_strField_value"),
        "arr1" -> BSONArray(1, 2, 3, 4, 5)
    )

    val doc = BSONDocument(
        "intField1" -> 12345,
        "subdoc1" -> BSONDocument("intField" -> 123, "strField" -> "A string"),
        "id" -> BSONObjectID("507f1f77bcf86cd799439011"),
        "subdoc2" -> subdoc2
    )

    "document \\ field_name" should "return ExtendedBSONDocument(BSONInteger(value)) for an integer field" in {
        val result = (doc \ "intField1")

        result should be (ExtendedBSONDocument(Some(BSONInteger(12345))))
    }

    "document \\ non_existent_field_name" should "return ExtendedBSONDocument(None)" in {
        val result = (doc \ "non_existent_field_name")

        result should be (ExtendedBSONDocument(Option.empty))
    }

    "document \\ field_name" should "return ExtendedBSONDocument(BSONDocument(...)) for a BSONDocument field" in {
        val result = (doc \ "subdoc2")

        result should be (extendedBSONDocument(subdoc2))
    }

    "document \\ integer_index" should "return element at the specified index for a BSONArray" in {
        val result = (ExtendedBSONDocument(Some(BSONArray(0, 1, 2, 3, 4, 5))) \ 3)

        result should be (ExtendedBSONDocument(Some(BSONInteger(3))))
    }

    "ExtendedBSONDocument(Some(aBSONValueInstance)).as[T]" should "return None for any T incompatible with the BSONValue instance" in {
        val result = (doc \ "intField1").as[String]

        result should be (Option.empty)
    }

    "(document \\ complex path).as[T]" should "return the value of the selected BSON field" in {
        ((doc \ "subdoc2" \ "subdoc2_1" \ "strField").as[String]) should be (Some("subdoc2_1_strField_value"))
    }

}
