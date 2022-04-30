package example

import bookstore.domain.books._
import org.scalacheck.Gen

object generators {

  val nonEmptyStringGen: Gen[String] =
    Gen.chooseNum(21, 40).flatMap { n =>
      Gen.buildableOfN[String, Char](n, Gen.alphaChar)
    }

  val bookGen: Gen[Book] =
    for {
      bookId <- Gen.choose(1, 10000)
      title <- nonEmptyStringGen
      isbn <- nonEmptyStringGen
      authorId <- Gen.choose(1, 1000)
    } yield Book(bookId, title, isbn, authorId)

}
