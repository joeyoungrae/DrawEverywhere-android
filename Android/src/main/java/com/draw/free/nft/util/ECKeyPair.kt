package com.draw.free.nft.util

import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.util.encoders.Hex

class ECKeyPair(publicKey: ECPublicKey, privateKey: ECPrivateKey) {
    private val privateKey: ECPrivateKey
    private val publicKey: ECPublicKey
    val public: ECPublicKey
        get() = publicKey
    val private: ECPrivateKey
        get() = privateKey

    fun getPublicBinary(encoded: Boolean): ByteArray {
        return publicKey.getQ().getEncoded(encoded)
    }

    val privateBinary: ByteArray
        get() = privateKey.getD().toByteArray()

    fun getPublicHex(encoded: Boolean): String {
        return Hex.toHexString(getPublicBinary(encoded))
    }

    val privateHex: String
        get() = Hex.toHexString(privateBinary)

    init {
        this.publicKey = publicKey
        this.privateKey = privateKey
    }
}