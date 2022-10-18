package com.draw.free.nft.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.network.BaseResponse
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.network.RetrofitClient
import com.draw.free.nft.util.Argon2kt.Companion.verify
import com.draw.free.nft.util.Base58.Companion.decodeBase58
import com.draw.free.nft.util.Ecies
import com.draw.free.nft.util.Ecies.Companion.decodeHex
import com.draw.free.nft.util.Ecies.Companion.toHex
import com.draw.free.nft.util.LocalEncryption.Companion.decrypt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber

class PasswordAndProcessingViewModel : ViewModel() {

    var mPassword = MutableLiveData<String>()

    var password: String? = null
    var afterProcessing: ToNextWork? = null
    var failedProcessing: ToNextWork? = null

    var serverPublicKey: String? = null

    private val handler = Handler(Looper.getMainLooper())
    val retrofitClient = RetrofitClient.getSolanaService()

    private var unique: String? = null

    // minting
    var metadata: ParcelableNftMetadata? = null

    // selling
    var sellData: ParcelableNftSelling? = null

    // buying & endingSale & settlingBill
    var auctionCache: String? = null

    var startTime: Long = -1

    fun checkPassword(): Boolean {
        return if (!Global.prefs.walletPassword.isNullOrEmpty()) {
            if (!mPassword.value.isNullOrEmpty()) {
                verify(Global.prefs.walletPassword!!, mPassword.value!!)
            } else {
                Global.makeToast("비밀번호를 입력해주세요")
                false
            }
        } else {
            Timber.e("클라에 저장된 비밀번호 없음!")
            false
        }
    }

    private fun addRunnableToHandler() {
        if (startTime == -1L) {
            startTime = System.currentTimeMillis()
        }

        val checkStatus = Runnable {
            retrofitClient.getStatus(unique!!).enqueue(BaseResponse { statusResponse ->
                handler.removeCallbacksAndMessages(null)
                Timber.e("statusResponse: $statusResponse")
                Timber.e("body: ${statusResponse.body()}")
                if (statusResponse.isSuccessful && statusResponse.code() == 200) {
                    if (statusResponse.body()!!.status == "finished") {
                        // 완료 후 실행할 것
                        afterProcessing?.next()
                        return@BaseResponse true
                    } else {
                        Timber.e("뭐임 ${statusResponse.body()!!.status}" )
                    }
                } else {
                    Timber.e("서버 응답 느림(504 error) 또는 아직 처리 완료 안됨")
                }

                addRunnableToHandler();

                return@BaseResponse true
            })
        }

        if (startTime != -1L && System.currentTimeMillis() - startTime < 180000L) {
            handler.postDelayed(checkStatus, 15000)
        } else {
            handler.removeCallbacksAndMessages(null)
            failedProcessing?.next();
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun mintNft() {
        // 지갑 SecretKey 가져오기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = decrypt(cipher!!, password!!)

        // 서버 요청

        val postId = MultipartBody.Part.createFormData("post_id", metadata!!.post!!)
        val name = MultipartBody.Part.createFormData("name", metadata!!.name!!)
        val symbol = MultipartBody.Part.createFormData("symbol", metadata!!.symbol!!)
        val desc = MultipartBody.Part.createFormData("desc", metadata!!.description!!)
        val creativeFee = MultipartBody.Part.createFormData(
            "creative_fee",
            metadata!!.sellerFeeBasisPoints!!
        )
        // 서버 퍼블릭 키로 다시 암호화
        val secretKeyCipher =
            Ecies.encrypt(serverPublicKey!!.decodeHex(), secretKey.decodeBase58().toByteArray())
        val secretKeyCipherHex =
            MultipartBody.Part.createFormData("secret_key_cipher", secretKeyCipher.toHex())

        // api 요청
         retrofitClient.mintNft(postId, name, symbol, desc, creativeFee, secretKeyCipherHex)
            .enqueue(BaseResponse { response ->
                if (response.code() == 202) {
                    // 상태 확인 키값
                    unique = response.body()!!.message
                    // 처리상태 확인
                    addRunnableToHandler()

                    return@BaseResponse true
                }

                return@BaseResponse false
            })
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun sellNft() {
        // 지갑 SecretKey 가져오기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = decrypt(cipher!!, password!!)

        // 서버 요청

        val fMint = MultipartBody.Part.createFormData("mint", sellData!!.mint)
        val fHolder = MultipartBody.Part.createFormData("holder", sellData!!.holder)
        val fPrice = MultipartBody.Part.createFormData("price", sellData!!.price!!)

        // 서버 퍼블릭 키로 다시 암호화
        val secretKeyCipher =
            Ecies.encrypt(serverPublicKey!!.decodeHex(), secretKey.decodeBase58().toByteArray())
        val secretKeyCipherHex =
            MultipartBody.Part.createFormData("secret_key_cipher", secretKeyCipher.toHex())

        // api 요청
        retrofitClient.sellNft(fMint, fHolder, fPrice, secretKeyCipherHex)
            .enqueue(BaseResponse { response ->
                if (response.code() == 202) {
                    // 상태 확인 키값
                    unique = response.body()!!.message
                    // 처리 상태 확인
                    addRunnableToHandler()
                    return@BaseResponse true
                }

                return@BaseResponse false
            })

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun buyNft() {
        // 지갑 SecretKey 가져오기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = decrypt(cipher!!, password!!)

        // 서버 요청

        val fAuctionCache = MultipartBody.Part.createFormData("auction_cache", auctionCache!!)

        // 서버 퍼블릭 키로 다시 암호화
        val secretKeyCipher =
            Ecies.encrypt(serverPublicKey!!.decodeHex(), secretKey.decodeBase58().toByteArray())
        val secretKeyCipherHex =
            MultipartBody.Part.createFormData("secret_key_cipher", secretKeyCipher.toHex())

        // api 요청
        retrofitClient.buyNft(fAuctionCache, secretKeyCipherHex).enqueue(BaseResponse { response ->
            if (response.code() == 202) {
                // 상태 확인 키값
                unique = response.body()!!.message
                // 처리 상태 확인
                addRunnableToHandler()
            }

            return@BaseResponse false

        })

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun endSale() {
        // 지갑 SecretKey 가져오기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = decrypt(cipher!!, password!!)

        // 서버 요청

        val fAuctionCache = MultipartBody.Part.createFormData("auction_cache", auctionCache!!)

        // 서버 퍼블릭 키로 다시 암호화
        val secretKeyCipher =
            Ecies.encrypt(serverPublicKey!!.decodeHex(), secretKey.decodeBase58().toByteArray())
        val secretKeyCipherHex =
            MultipartBody.Part.createFormData("secret_key_cipher", secretKeyCipher.toHex())

        // api 요청

        retrofitClient.endSale(fAuctionCache, secretKeyCipherHex).enqueue(BaseResponse { response ->
            if (response.code() == 202) {
                // 상태 확인 키값
                unique = response.body()!!.message
                // 처리 상태 확인
                addRunnableToHandler()
                return@BaseResponse true
            }
            return@BaseResponse false
        })

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun settleBill() {
        // 지갑 SecretKey 가져오기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = decrypt(cipher!!, password!!)

        // 서버 요청
        val fAuctionCache = MultipartBody.Part.createFormData("auction_cache", auctionCache!!)

        // 서버 퍼블릭 키로 다시 암호화
        val secretKeyCipher =
            Ecies.encrypt(serverPublicKey!!.decodeHex(), secretKey.decodeBase58().toByteArray())
        val secretKeyCipherHex =
            MultipartBody.Part.createFormData("secret_key_cipher", secretKeyCipher.toHex())

        // api 요청
        retrofitClient.settleBill(fAuctionCache, secretKeyCipherHex)
            .enqueue(BaseResponse { response ->
                if (response.code() == 202) {
                    // 상태 확인 키값
                    unique = response.body()!!.message
                    // 처리 상태 확인
                    addRunnableToHandler()
                    return@BaseResponse true
                }

                return@BaseResponse false
            })
    }
}