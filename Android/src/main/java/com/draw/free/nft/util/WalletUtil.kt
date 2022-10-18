package com.draw.free.nft.util

import com.draw.free.Global
import com.draw.free.nft.util.Argon2kt.Companion.toByteArray
import com.draw.free.nft.util.Base58.Companion.decodeBase58
import com.draw.free.nft.util.Base58.Companion.encodeBase58
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.soywiz.krypto.AES
import com.soywiz.krypto.PBKDF2
import com.soywiz.krypto.Padding
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.util.encoders.Hex
import timber.log.Timber
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.NoSuchPaddingException


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


class LocalEncryption {
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


class Ecies {
    companion object {
        private const val CURVE_NAME = "secp256k1"
        private const val UNCOMPRESSED_PUBLIC_KEY_SIZE = 65
        private const val AES_IV_LENGTH = 16
        private const val AES_TAG_LENGTH = 16
        private const val AES_IV_PLUS_TAG_LENGTH = AES_IV_LENGTH + AES_TAG_LENGTH
        private const val SECRET_KEY_LENGTH = 32
        private val SECURE_RANDOM: SecureRandom = SecureRandom()

        @OptIn(ExperimentalUnsignedTypes::class)
        fun ByteArray.toHex(): String = toUByteArray().joinToString("") {
            it.toString(radix = 16).padStart(2, '0')
        }

        fun String.decodeHex(): ByteArray {
            check(length % 2 == 0) { "Must have an even length" }

            return chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }

        @Throws(NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
        fun generateEphemeralKey(ecSpec: ECNamedCurveParameterSpec): KeyPair {
            val g: KeyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
            g.initialize(ecSpec, SECURE_RANDOM)
            return g.generateKeyPair()
        }


        fun generateEcKeyPair(): ECKeyPair {
            val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME)
            val g: KeyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
            g.initialize(ecSpec, SECURE_RANDOM)
            val keyPair: KeyPair = g.generateKeyPair()
            return ECKeyPair(
                keyPair.getPublic() as ECPublicKey,
                keyPair.getPrivate() as ECPrivateKey
            )
        }


        fun encrypt(publicKeyHex: String?, message: String): String {
            val publicKey: ByteArray = Hex.decode(publicKeyHex)
            val encrypt = encrypt(publicKey, message.toByteArray(StandardCharsets.UTF_8))
            return Hex.toHexString(encrypt)
        }


        fun decrypt(privateKeyHex: String?, ciphertext: String?): String {
            val privateKey: ByteArray = Hex.decode(privateKeyHex)
            val cipherBytes: ByteArray = Hex.decode(ciphertext)
            return String(decrypt(privateKey, cipherBytes), StandardCharsets.UTF_8)
        }


        fun encrypt(publicKeyBytes: ByteArray, message: ByteArray): ByteArray {
            val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME)
            val pair: KeyPair = generateEphemeralKey(ecSpec)
            val ephemeralPrivKey: ECPrivateKey = pair.getPrivate() as ECPrivateKey
            val ephemeralPubKey: ECPublicKey = pair.getPublic() as ECPublicKey

            //generate receiver PK
            val keyFactory: KeyFactory = keyFactory
            val curvedParams = ECNamedCurveSpec(CURVE_NAME, ecSpec.curve, ecSpec.g, ecSpec.n)
            val publicKey: ECPublicKey = getEcPublicKey(curvedParams, publicKeyBytes, keyFactory)

            //Derive shared secret
            val uncompressed: ByteArray = ephemeralPubKey.getQ().getEncoded(false)
            val multiply: ByteArray =
                publicKey.getQ().multiply(ephemeralPrivKey.getD()).getEncoded(false)
            val aesKey = hkdf(uncompressed, multiply)

            // AES encryption
            return aesEncrypt(message, ephemeralPubKey, aesKey)
        }


        fun decrypt(privateKeyBytes: ByteArray?, cipherBytes: ByteArray): ByteArray {
            val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME)
            val keyFactory: KeyFactory = keyFactory
            val curvedParams = ECNamedCurveSpec(CURVE_NAME, ecSpec.curve, ecSpec.g, ecSpec.n)

            //generate receiver private key
            val privateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKeyBytes), curvedParams)
            val receiverPrivKey: ECPrivateKey =
                keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey

            //get sender pub key
            val senderPubKeyByte = Arrays.copyOf(cipherBytes, UNCOMPRESSED_PUBLIC_KEY_SIZE)
            val senderPubKey: ECPublicKey =
                getEcPublicKey(curvedParams, senderPubKeyByte, keyFactory)

            //decapsulate
            val uncompressed: ByteArray = senderPubKey.getQ().getEncoded(false)
            val multiply: ByteArray =
                senderPubKey.getQ().multiply(receiverPrivKey.d).getEncoded(false)
            val aesKey = hkdf(uncompressed, multiply)

            // AES decryption
            return aesDecrypt(cipherBytes, aesKey)
        }

        @get:Throws(NoSuchAlgorithmException::class)
        private val keyFactory: KeyFactory
            get() = KeyFactory.getInstance("EC", BouncyCastleProvider())

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            NoSuchProviderException::class,
            InvalidCipherTextException::class
        )
        private fun aesEncrypt(
            message: ByteArray,
            ephemeralPubKey: ECPublicKey,
            aesKey: ByteArray
        ): ByteArray {
            val aesgcmBlockCipher = AESGCMBlockCipher()
            val nonce = ByteArray(AES_IV_LENGTH)
            SECURE_RANDOM.nextBytes(nonce)
            val parametersWithIV = ParametersWithIV(KeyParameter(aesKey), nonce)
            aesgcmBlockCipher.init(true, parametersWithIV)
            val outputSize: Int = aesgcmBlockCipher.getOutputSize(message.size)
            var encrypted = ByteArray(outputSize)
            val pos: Int = aesgcmBlockCipher.processBytes(message, 0, message.size, encrypted, 0)
            aesgcmBlockCipher.doFinal(encrypted, pos)
            val tag = Arrays.copyOfRange(encrypted, encrypted.size - nonce.size, encrypted.size)
            encrypted = Arrays.copyOfRange(encrypted, 0, encrypted.size - tag.size)
            val ephemeralPkUncompressed: ByteArray = ephemeralPubKey.getQ().getEncoded(false)
            return org.bouncycastle.util.Arrays.concatenate(
                ephemeralPkUncompressed,
                nonce,
                tag,
                encrypted
            )
        }

        @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            NoSuchProviderException::class,
            InvalidCipherTextException::class
        )
        private fun aesDecrypt(inputBytes: ByteArray, aesKey: ByteArray): ByteArray {
            val encrypted =
                Arrays.copyOfRange(inputBytes, UNCOMPRESSED_PUBLIC_KEY_SIZE, inputBytes.size)
            val nonce = Arrays.copyOf(encrypted, AES_IV_LENGTH)
            val tag = Arrays.copyOfRange(encrypted, AES_IV_LENGTH, AES_IV_PLUS_TAG_LENGTH)
            val ciphered = Arrays.copyOfRange(encrypted, AES_IV_PLUS_TAG_LENGTH, encrypted.size)
            val aesgcmBlockCipher = AESGCMBlockCipher()
            val parametersWithIV = ParametersWithIV(KeyParameter(aesKey), nonce)
            aesgcmBlockCipher.init(false, parametersWithIV)
            val outputSize: Int = aesgcmBlockCipher.getOutputSize(ciphered.size + tag.size)
            val decrypted = ByteArray(outputSize)
            var pos: Int = aesgcmBlockCipher.processBytes(ciphered, 0, ciphered.size, decrypted, 0)
            pos += aesgcmBlockCipher.processBytes(tag, 0, tag.size, decrypted, pos)
            aesgcmBlockCipher.doFinal(decrypted, pos)
            return decrypted
        }

        private fun hkdf(uncompressed: ByteArray, multiply: ByteArray): ByteArray {
            val master = org.bouncycastle.util.Arrays.concatenate(uncompressed, multiply)
            val hkdfBytesGenerator = HKDFBytesGenerator(SHA256Digest())
            hkdfBytesGenerator.init(HKDFParameters(master, null, null))
            val aesKey = ByteArray(SECRET_KEY_LENGTH)
            hkdfBytesGenerator.generateBytes(aesKey, 0, aesKey.size)
            return aesKey
        }

        @Throws(InvalidKeySpecException::class)
        private fun getEcPublicKey(
            curvedParams: ECNamedCurveSpec,
            senderPubKeyByte: ByteArray,
            keyFactory: KeyFactory
        ): ECPublicKey {
            val point = ECPointUtil.decodePoint(curvedParams.curve, senderPubKeyByte)
            val pubKeySpec = ECPublicKeySpec(point, curvedParams)
            return keyFactory.generatePublic(pubKeySpec) as ECPublicKey
        }
    }
}