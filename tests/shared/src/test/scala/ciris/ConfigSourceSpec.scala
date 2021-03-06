package ciris

import org.scalacheck.Gen

import scala.util.Try

final class ConfigSourceSpec extends PropertySpec {
  "ConfigSource" when {
    "converting to String" should {
      "include the key type" in {
        forAll { keyType: String =>
          val configKey = ConfigKeyType[String](keyType)
          ConfigSource(configKey)(Right.apply).toString shouldBe s"ConfigSource($configKey)"
        }
      }
    }

    "created from entries" should {
      "succeed for keys in the entries" in {
        forAll { (keyType: String, entries: Seq[(Int, String)]) =>
          whenever(entries.nonEmpty) {
            val source = ConfigSource.fromEntries(ConfigKeyType[Int](keyType))(entries: _*)
            forAll(Gen.oneOf(entries)) { case (key, _) =>
              source.read(key).value shouldBe a[Right[_, _]]
            }
          }
        }
      }

      "fail for keys not in the entries" in {
        forAll { (keyType: String, entries: Seq[(Int, String)]) =>
          val source = ConfigSource.fromEntries(ConfigKeyType[Int](keyType))(entries: _*)
          val exists = entries.map { case (key, _) => key }.toSet

          forAll(minSuccessful(10)) { key: Int =>
            whenever(!exists(key)) {
              source.read(key).value shouldBe a[Left[_, _]]
            }
          }
        }
      }
    }

    "created from a Try" should {
      "succeed if the Try succeeds" in {
        forAll { (keyType: String, key: String) =>
          val source = ConfigSource.fromTry(ConfigKeyType[String](keyType))(key => Try(key))
          source.read(key).value shouldBe Right(key)
        }
      }

      "fail if the Try fails" in {
        forAll { (keyType: String, key: String) =>
          val source = ConfigSource.fromTry(ConfigKeyType[String](keyType))(_ => Try(throw new Error))
          source.read(key).value shouldBe a[Left[_, _]]
        }
      }
    }

    "always empty" should {
      "fail for any key" in {
        val source = ConfigSource.empty(ConfigKeyType[String]("key"))
        forAll { (key: String) =>
          source.read(key).value shouldBe a[Left[_, _]]
        }
      }
    }

    "created from an IndexedSeq" should {
      "succeed if the index exists and value is of expected type" in {
        forAll { (keyType: String, indexedSeq: IndexedSeq[String]) =>
          whenever(indexedSeq.nonEmpty) {
            val source = ConfigSource.byIndex(ConfigKeyType[Int](keyType))(indexedSeq)
            forAll(Gen.chooseNum(0, indexedSeq.length - 1)) { index =>
              source.read(index).value shouldBe a[Right[_, _]]
            }
          }
        }
      }

      "fail if the index does not exist" in {
        forAll { (keyType: String, indexedSeq: IndexedSeq[String]) =>
          val source = ConfigSource.byIndex(ConfigKeyType[Int](keyType))(indexedSeq)

          forAll {
            Gen.oneOf(
              Gen.chooseNum(Int.MinValue, -1),
              Gen.chooseNum(indexedSeq.length, Int.MaxValue)
            )
          } { index =>
            source.read(index).value shouldBe a[Left[_, _]]
          }
        }
      }
    }

    "catching non-fatal exceptions" should {
      "succeed if an exception is not thrown" in {
        forAll { (keyType: String, key: String) =>
          val source = ConfigSource.catchNonFatal(ConfigKeyType[String](keyType))(identity)
          source.read(key).value shouldBe Right(key)
        }
      }

      "fail if a non-fatal exception is thrown" in {
        forAll { (keyType: String, key: String) =>
          val source = ConfigSource.catchNonFatal(ConfigKeyType[String](keyType))(_ => throw new Error)
          source.read(key).value shouldBe a[Left[_, _]]
        }
      }
    }
  }
}
