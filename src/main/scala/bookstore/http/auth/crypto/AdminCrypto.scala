package bookstore.http.auth.crypto

import org.reactormonk.PrivateKey
import scala.io.Codec
import org.reactormonk.CryptoBits
import scala.util.Random

object AdminCrypto {
  val key = PrivateKey(Codec.toUTF8(Random.alphanumeric.take(20).mkString("")))
  val crypto = CryptoBits(key)
  val clock = java.time.Clock.systemUTC
}
