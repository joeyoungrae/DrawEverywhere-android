package com.draw.free.network.dao

import com.draw.free.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface SolanaService {

    @GET("api/solana/nfts/post/{post_id}")
    fun getNftByPostId(@Path("post_id") postId : String) : Call<Nft>

    @GET("api/solana/nfts/mint/{mint}")
    fun getNftByMint(@Path("mint") mint : String) : Call<Nft>

    @GET("api/solana/nfts/auctions/list/{offset}/{size}")
    suspend fun getAuctionsAll(@Path("offset") offset: String, @Path("size") size: Int): AuctionListingResponse


    @GET("api/solana/nfts/auctions/list/{offset}/{size}/status/started")
    suspend fun getAuctionsOnSale(@Path("offset") offset: String, @Path("size") size: Int): AuctionListingResponse

    @GET("api/solana/nfts/auctions/list/{offset}/{size}/status/ended/canceled")
    suspend fun getAuctionsCancel(@Path("offset") offset: String, @Path("size") size: Int): AuctionListingResponse

    @GET("api/solana/nfts/auctions/list/{offset}/{size}/status/ended/sold")
    suspend fun getAuctionsSold(@Path("offset") offset: String, @Path("size") size: Int): AuctionListingResponse

    @GET("api/solana/wallets/user/{account_id}")
    suspend fun getWalletAddressByUser(@Path("account_id") accountId: String): String?

    @GET("api/solana/nfts/list/{offset}/{size}")
    suspend fun getRecentlyMintedNfts(@Path("offset") offset: String,
                                      @Path("size") size: Int): NftListingResponse

    @GET("api/solana/nfts/list/owner/{address}")
    suspend fun getAddressOwnedNfts(@Path("address") address: String): NftListingResponse

    @GET("api/solana/nfts/list/{offset}/{size}/minter/{address}")
    suspend fun getAddressMintedNfts(@Path("offset") offset: String,
                                     @Path("size") size: Int,
                                     @Path("address") address: String): NftListingResponse

    @GET("api/solana/nfts/auctions/list/{offset}/{size}/auctioneer/{address}")
    suspend fun getAddressAuctions(@Path("offset") offset: String,
                                   @Path("size") size: Int,
                                   @Path("address") address: String): AuctionListingResponse

    @GET("api/solana/balance")
    suspend fun getBalance(@Query("address") address: String): Double

    @Multipart
    @POST("api/solana/nfts/minting/cost")
    suspend fun getExpectedMintingCost(@Part post_id: MultipartBody.Part,
                                       @Part name: MultipartBody.Part,
                                       @Part symbol: MultipartBody.Part,
                                       @Part desc: MultipartBody.Part,
                                       @Part creative_fee: MultipartBody.Part): Double

    @Multipart
    @POST("api/solana/nfts/selling/cost")
    suspend fun getExpectedSellingCost(@Part address: MultipartBody.Part): Double

    @Multipart
    @POST("api/solana/nfts/buying/cost")
    suspend fun getExpectedBuyingCost(@Part auction_cache: MultipartBody.Part): Double

    @POST("api/solana/nfts/end-sale/cost")
    suspend fun getExpectedEndingSaleCost(): Double

    @POST("api/solana/nfts/settle-a-bill/cost")
    suspend fun getExpectedSettlingBillCost(): Double


    @GET("api/solana/nfts/auctions/mint/{mint}")
    fun getStartedAuctionByMint(@Path("mint") mint: String): Call<Auction>

    @Multipart
    @POST("api/solana/nfts/mint")
    fun mintNft(@Part post_id: MultipartBody.Part,
                @Part name: MultipartBody.Part,
                @Part symbol: MultipartBody.Part,
                @Part desc: MultipartBody.Part,
                @Part creative_fee: MultipartBody.Part,
                @Part secret_key_cipher: MultipartBody.Part): Call<MessageResponse>

    @Multipart
    @POST("api/solana/nfts/sell")
    fun sellNft(@Part mint: MultipartBody.Part,
                @Part holder: MultipartBody.Part,
                @Part price: MultipartBody.Part,
                @Part secret_key_cipher: MultipartBody.Part): Call<MessageResponse>

    @Multipart
    @POST("api/solana/nfts/buy")
    fun buyNft(@Part auction_cache: MultipartBody.Part,
               @Part secret_key_cipher: MultipartBody.Part): Call<MessageResponse>

    @Multipart
    @PUT("api/solana/nfts/sell")
    fun endSale(@Part auction_cache: MultipartBody.Part,
                @Part secret_key_cipher: MultipartBody.Part): Call<MessageResponse>

    @Multipart
    @POST("api/solana/nfts/settle")
    fun settleBill(@Part auction_cache: MultipartBody.Part,
                   @Part secret_key_cipher: MultipartBody.Part): Call<MessageResponse>


    @POST("api/solana/nfts/status/{unique}")
    fun getStatus(@Path("unique") unique: String): Call<StatusResponse>

    @FormUrlEncoded
    @PUT("api/solana/wallets")
    fun connectWallet(@Field("seed_phrase") seedPhrase : String, @Field("secret_key_cipher") secretKeyPhrase : String) : Call<WalletChangeResponse>

}