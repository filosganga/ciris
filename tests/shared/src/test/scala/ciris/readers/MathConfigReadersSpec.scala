package ciris.readers

import ciris.PropertySpec

final class MathConfigReadersSpec extends PropertySpec {
  "MathConfigReaders" when {
    "reading a BigInt" should {
      "successfully read BigInt values" in {
        forAll { bigInt: BigInt =>
          readValue[BigInt](bigInt.toString) shouldBe Right(bigInt)
        }
      }

      "return a failure for other values" in {
        forAll { string: String =>
          whenever(fails(BigInt(string))) {
            readValue[BigInt](string) shouldBe a[Left[_, _]]
          }
        }
      }
    }

    "reading a BigDecimal" should {
      "successfully read BigDecimal values" in {
        forAll { double: Double =>
          val doubleString = double.toString
          readValue[BigDecimal](doubleString) shouldBe Right(BigDecimal(doubleString))
        }
      }

      "return a failure for other values" in {
        forAll { string: String =>
          whenever(fails(BigDecimal(string))) {
            readValue[BigDecimal](string) shouldBe a[Left[_, _]]
          }
        }
      }
    }
  }
}
