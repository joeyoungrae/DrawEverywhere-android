package com.draw.free.util

import com.draw.free.Global
import com.draw.free.util.Argon2kt.Companion.toByteArray
import com.draw.free.util.Base58.Companion.decodeBase58
import com.draw.free.util.Base58.Companion.encodeBase58
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.soywiz.krypto.AES
import com.soywiz.krypto.PBKDF2
import com.soywiz.krypto.Padding
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*


class Argon2kt {
    companion object {
        // Raw hash: hashResult.rawHashAsHexadecimal()
        // Encoded string: hashResult.encodedOutputAsString()
        private val argon2Kt = Argon2Kt()

        fun getEncodedString(password: String): String {
            return argon2Kt.hash(
                mode = Argon2Mode.ARGON2_I,
                password = password.toByteArray(Charsets.UTF_8),
                salt = UUID.randomUUID().toByteArray(),
                tCostInIterations = 5,
                mCostInKibibyte = 65536
            ).encodedOutputAsString()
        }

        fun verify(encodedString: String, password: String): Boolean {
            return argon2Kt.verify(
                mode = Argon2Mode.ARGON2_I,
                encoded = encodedString,
                password = password.toByteArray(Charsets.UTF_8)
            )
        }

        fun UUID.toByteArray(): ByteArray {
            val b = ByteBuffer.wrap(ByteArray(16))
            b.putLong(mostSignificantBits)
            b.putLong(leastSignificantBits)
            return b.array()
        }
    }
}


class Encryption {
    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun encrypt(secretKey: String, password: String) : String {
            val salt = UUID.randomUUID().toByteArray()

            // salt 값 저장
            Global.prefs.walletSalt = salt.toUByteArray().encodeBase58()

            val newKey = PBKDF2.pbkdf2WithHmacSHA256(
                password.toByteArray(Charsets.UTF_8),
                salt,
                100000, 256
            )
            val cipher = AES.encryptAesCbc(
                secretKey.toByteArray(Charsets.UTF_8),
                newKey,
                ByteArray(16),
                Padding.PKCS7Padding
            )
            return cipher.toUByteArray().encodeBase58()
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        fun decrypt(cipher: String, password: String) : String {
            val salt = Global.prefs.walletSalt!!.decodeBase58().toByteArray()

            val newKey = PBKDF2.pbkdf2WithHmacSHA256(
                password.toByteArray(Charsets.UTF_8),
                salt,
                100000, 256
            )
            val plainText = AES.decryptAesCbc(
                cipher.decodeBase58().toByteArray(),
                newKey,
                ByteArray(16),
                Padding.PKCS7Padding
            )
            return String(plainText)
        }
    }
}


class U8Array {
    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun IntArray.toUIntArray(): UIntArray {
            val input = copyOf(size)
            if (input.isEmpty()) {
                return UIntArray(0)
            }
            return input.map { it.toUInt() }.toUIntArray()
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        fun UByteArray.toUIntArray(): UIntArray {
            val input = copyOf(size)
            if (input.isEmpty()) {
                return UIntArray(0)
            }
            return input.foldIndexed(UIntArray(input.size)) { i, a, v -> a.apply { set(i, v.toUInt()) }}
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        fun UIntArray.toUByteArray(): UByteArray {
            val input = copyOf(size)
            if (input.isEmpty()) {
                return UByteArray(0)
            }
            return input.foldIndexed(UByteArray(input.size)) { i, a, v -> a.apply { set(i, v.toUByte()) } }
        }
    }
}

class Base58 {
    companion object {

        private const val ENCODED_ZERO = '1'

        private const val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        private val alphabetIndices by lazy {
            IntArray(128) { alphabet.indexOf(it.toChar()) }
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        fun UByteArray.encodeBase58(): String {
            val input = copyOf(size) // since we modify it in-place
            if (input.isEmpty()) {
                return ""
            }
            // Count leading zeros.
            var zeros = 0
            while (zeros < input.size && input[zeros].toInt() == 0) {
                ++zeros
            }
            // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
            val encoded = CharArray(input.size * 2) // upper bound
            var outputStart = encoded.size
            var inputStart = zeros
            while (inputStart < input.size) {
                encoded[--outputStart] = alphabet[divmod(input, inputStart.toUInt(), 256.toUInt(), 58.toUInt()).toInt()]
                if (input[inputStart].toInt() == 0) {
                    ++inputStart // optimization - skip leading zeros
                }
            }
            // Preserve exactly as many leading encoded zeros in output as there were leading zeros in data.
            while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
                ++outputStart
            }
            while (--zeros >= 0) {
                encoded[--outputStart] = ENCODED_ZERO
            }
            // Return encoded string (including encoded leading zeros).
            return String(encoded, outputStart, encoded.size - outputStart)
        }


        @OptIn(ExperimentalUnsignedTypes::class)
        @Throws(NumberFormatException::class)
        fun String.decodeBase58(): UByteArray {
            if (isEmpty()) {
                return UByteArray(0)
            }
            // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
            val input58 = UByteArray(length)
            for (i in 0 until length) {
                val c = this[i]
                val digit = if (c.code < 128) alphabetIndices[c.code] else -1
                if (digit < 0) {
                    throw NumberFormatException("Illegal character $c at position $i")
                }
                input58[i] = digit.toUByte()
            }
            // Count leading zeros.
            var zeros = 0
            while (zeros < input58.size && input58[zeros].toInt() == 0) {
                ++zeros
            }
            // Convert base-58 digits to base-256 digits.
            val decoded = UByteArray(length)
            var outputStart = decoded.size
            var inputStart = zeros
            while (inputStart < input58.size) {
                decoded[--outputStart] = divmod(input58, inputStart.toUInt(), 58.toUInt(), 256.toUInt()).toUByte()
                if (input58[inputStart].toInt() == 0) {
                    ++inputStart // optimization - skip leading zeros
                }
            }
            // Ignore extra leading zeroes that were added during the calculation.
            while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
                ++outputStart
            }
            // Return decoded data (including original number of leading zeros).
            return decoded.copyOfRange(outputStart - zeros, decoded.size)
        }


        @OptIn(ExperimentalUnsignedTypes::class)
        private fun divmod(number: UByteArray, firstDigit: UInt, base: UInt, divisor: UInt): UInt {
            // this is just long division which accounts for the base of the input digits
            var remainder = 0.toUInt()
            for (i in firstDigit until number.size.toUInt()) {
                val digit = number[i.toInt()].toUByte()
                val temp = remainder * base + digit
                number[i.toInt()] = (temp / divisor).toUByte()
                remainder = temp % divisor
            }
            return remainder
        }

    }
}