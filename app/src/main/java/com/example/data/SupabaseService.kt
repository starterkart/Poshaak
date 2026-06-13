package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ==========================================
// SUPABASE API MODELS
// ==========================================

data class SupabaseSignUpUserMetadata(
    val full_name: String,
    val is_guest: Boolean = false
)

data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: SupabaseSignUpUserMetadata
)

data class SupabaseLoginRequest(
    val email: String,
    val password: String
)

data class SupabaseForgotRequest(
    val email: String
)

data class SupabaseUser(
    val id: String,
    val email: String?,
    val user_metadata: Map<String, Any>?
)

data class SupabaseAuthResponse(
    val access_token: String?,
    val refresh_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val user: SupabaseUser?
)

// DB Models for PostgREST mapping (we will map dynamically to/from entities)
// Retrofit interface for Auth and Database
interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): SupabaseAuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Body request: SupabaseLoginRequest
    ): SupabaseAuthResponse

    @POST("auth/v1/recover")
    suspend fun forgotPassword(
        @Body request: SupabaseForgotRequest
    ): retrofit2.Response<Unit>
}

interface SupabaseDbApi {
    // ------------------------------------------
    // Products
    // ------------------------------------------
    @GET("rest/v1/products?select=*")
    suspend fun getProducts(
        @Header("Range") range: String = "0-99"
    ): List<Map<String, Any>>

    @POST("rest/v1/products")
    @Headers("Prefer: return=representation")
    suspend fun insertProducts(
        @Body products: List<Map<String, Any>>
    ): List<Map<String, Any>>

    // ------------------------------------------
    // Product Variants
    // ------------------------------------------
    @GET("rest/v1/product_variants?select=*")
    suspend fun getVariants(): List<Map<String, Any>>

    @POST("rest/v1/product_variants")
    @Headers("Prefer: return=representation")
    suspend fun insertVariants(
        @Body variants: List<Map<String, Any>>
    ): List<Map<String, Any>>

    @PATCH("rest/v1/product_variants")
    suspend fun updateVariantStock(
        @Query("id") idFilter: String, // e.g. "eq.15"
        @Body body: Map<String, Int> // e.g. {"stock": 42}
    ): retrofit2.Response<Unit>

    // ------------------------------------------
    // Wishlist
    // ------------------------------------------
    @GET("rest/v1/wishlist_items?select=*")
    suspend fun getWishlist(
        @Query("userEmail") emailFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/wishlist_items")
    @Headers("Prefer: return=representation")
    suspend fun insertWishlist(
        @Body item: Map<String, Any>
    ): List<Map<String, Any>>

    @DELETE("rest/v1/wishlist_items")
    suspend fun deleteWishlist(
        @Query("userEmail") emailFilter: String,
        @Query("productId") productFilter: String
    ): retrofit2.Response<Unit>

    // ------------------------------------------
    // Cart
    // ------------------------------------------
    @GET("rest/v1/cart_items?select=*")
    suspend fun getCart(
        @Query("userEmail") emailFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/cart_items")
    @Headers("Prefer: return=representation")
    suspend fun insertCartItem(
        @Body item: Map<String, Any>
    ): List<Map<String, Any>>

    @PATCH("rest/v1/cart_items")
    suspend fun updateCartQty(
        @Query("id") idFilter: String,
        @Body body: Map<String, Int>
    ): retrofit2.Response<Unit>

    @PATCH("rest/v1/cart_items")
    suspend fun updateCartSaveForLater(
        @Query("id") idFilter: String,
        @Body body: Map<String, Boolean>
    ): retrofit2.Response<Unit>

    @DELETE("rest/v1/cart_items")
    suspend fun deleteCartItem(
        @Query("id") idFilter: String
    ): retrofit2.Response<Unit>

    @DELETE("rest/v1/cart_items")
    suspend fun clearCart(
        @Query("userEmail") emailFilter: String
    ): retrofit2.Response<Unit>

    // ------------------------------------------
    // Addresses
    // ------------------------------------------
    @GET("rest/v1/addresses?select=*")
    suspend fun getAddresses(
        @Query("userEmail") emailFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/addresses")
    @Headers("Prefer: return=representation")
    suspend fun insertAddress(
        @Body address: Map<String, Any>
    ): List<Map<String, Any>>

    @PATCH("rest/v1/addresses")
    suspend fun updateAddressDefault(
        @Query("id") idFilter: String,
        @Body body: Map<String, Boolean>
    ): retrofit2.Response<Unit>

    @PATCH("rest/v1/addresses")
    suspend fun clearDefaultAddresses(
        @Query("userEmail") emailFilter: String,
        @Body body: Map<String, Boolean>
    ): retrofit2.Response<Unit>

    @DELETE("rest/v1/addresses")
    suspend fun deleteAddress(
        @Query("id") idFilter: String
    ): retrofit2.Response<Unit>

    // ------------------------------------------
    // Orders & Order Items
    // ------------------------------------------
    @GET("rest/v1/orders?select=*")
    suspend fun getOrders(
        @Query("userEmail") emailFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/orders")
    @Headers("Prefer: return=representation")
    suspend fun insertOrder(
        @Body order: Map<String, Any>
    ): List<Map<String, Any>>

    @PATCH("rest/v1/orders")
    suspend fun cancelOrder(
        @Query("id") idFilter: String,
        @Body body: Map<String, String> // e.g. {"status": "Cancelled"}
    ): retrofit2.Response<Unit>

    @GET("rest/v1/order_items?select=*")
    suspend fun getOrderItems(
        @Query("orderId") orderIdFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/order_items")
    suspend fun insertOrderItems(
        @Body items: List<Map<String, Any>>
    ): retrofit2.Response<Unit>

    // ------------------------------------------
    // Reviews
    // ------------------------------------------
    @GET("rest/v1/reviews?select=*")
    suspend fun getReviews(
        @Query("productId") idFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/reviews")
    @Headers("Prefer: return=representation")
    suspend fun insertReview(
        @Body review: Map<String, Any>
    ): List<Map<String, Any>>

    // ------------------------------------------
    // Notifications
    // ------------------------------------------
    @GET("rest/v1/notifications?select=*")
    suspend fun getNotifications(
        @Query("userEmail") emailFilter: String
    ): List<Map<String, Any>>

    @POST("rest/v1/notifications")
    @Headers("Prefer: return=representation")
    suspend fun insertNotification(
        @Body notification: Map<String, Any>
    ): List<Map<String, Any>>

    @PATCH("rest/v1/notifications")
    suspend fun markNotificationsRead(
        @Query("userEmail") emailFilter: String,
        @Body body: Map<String, Boolean>
    ): retrofit2.Response<Unit>
}

// Global Supabase Manager
object SupabaseManager {
    var userToken: String? = null

    // Determine if Supabase is properly configured in the project
    val isEnabled: Boolean by lazy {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        url.isNotBlank() && 
        key.isNotBlank() && 
        !url.contains("your-project-id") && 
        !key.contains("your-anon-key")
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer ${userToken ?: BuildConfig.SUPABASE_ANON_KEY}")
                    .header("Content-Type", "application/json")
                
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val authApi: SupabaseAuthApi? by lazy {
        if (!isEnabled) return@lazy null
        try {
            Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseAuthApi::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val dbApi: SupabaseDbApi? by lazy {
        if (!isEnabled) return@lazy null
        try {
            Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseDbApi::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
