package com.draw.free.nft.util

import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger

class ConvertUtils {
    fun toHexStringBytesPadded(privateKey: ECPrivateKey): String {
        return toHexStringBytesPadded(privateKey.getD())
    }

    fun toHexStringBytesPadded(privateKey: BigInteger): String {
        return toHexStringBytesPadded(privateKey.toByteArray())
    }

    fun toHexStringBytesPadded(privateKey: ByteArray): String {
        return if (privateKey[0] == 0.toByte()) {
            Hex.toHexString(privateKey.copyOfRange(1, privateKey.size))
        } else Hex.toHexString(privateKey)
    }
}