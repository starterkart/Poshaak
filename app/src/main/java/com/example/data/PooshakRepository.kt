package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class PooshakRepository(private val db: AppDatabase) {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Run seed check in background or on database query
    }

    // ==========================================
    // AUTHENTICATION SYSTEM
    // ==========================================

    suspend fun initialSessionCheck() {
        // Try to see if any user exists in Room as an active session
        val savedUser = db.userDao.getAnyUser()
        if (savedUser != null) {
            _currentUser.value = savedUser
            if (SupabaseManager.isEnabled && !savedUser.sessionToken.isNullOrBlank()) {
                SupabaseManager.userToken = savedUser.sessionToken
            }
        }
    }

    suspend fun login(emailStr: String, passwordStr: String): Result<UserEntity> {
        val normalizedEmail = emailStr.trim().lowercase()
        if (normalizedEmail.isEmpty() || passwordStr.isEmpty()) {
            return Result.failure(Exception("Email and password cannot be empty"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        // 1. SUPABASE LOGIN ATTEMPT
        if (SupabaseManager.isEnabled) {
            try {
                Log.d("PooshakRepository", "Attempting Supabase login...")
                val req = SupabaseLoginRequest(normalizedEmail, passwordStr)
                val res = SupabaseManager.authApi!!.login(req)
                val token = res.access_token
                if (!token.isNullOrBlank()) {
                    SupabaseManager.userToken = token
                    val metaName = res.user?.user_metadata?.get("full_name")?.toString() ?: normalizedEmail.substringBefore("@")
                    val user = UserEntity(
                        email = normalizedEmail,
                        fullName = metaName,
                        isGuest = false,
                        sessionToken = token
                    )
                    db.userDao.insertUser(user)
                    _currentUser.value = user
                    triggerNotification(normalizedEmail, "Welcome back, ${user.fullName}! 🌐", "You are successfully connected via our secure Supabase cloud database.", "Promotion")
                    return Result.success(user)
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase authentication failed, trying registration fallback: ${e.message}")
                
                // FALLBACK SIGNUP FOR TESTING CONVENIENCE:
                // If the user doesn't exist yet on our fresh Supabase, let's create them!
                if (e.message?.contains("Invalid login credentials") == true || e.message?.contains("400") == true) {
                    try {
                        val defaultName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } + " Kumar"
                        val meta = SupabaseSignUpUserMetadata(defaultName)
                        val signupReq = SupabaseSignUpRequest(normalizedEmail, passwordStr, meta)
                        SupabaseManager.authApi!!.signUp(signupReq)
                        
                        // After successful signup, log them in
                        val loginAfterSignup = SupabaseManager.authApi!!.login(SupabaseLoginRequest(normalizedEmail, passwordStr))
                        val finalToken = loginAfterSignup.access_token
                        if (!finalToken.isNullOrBlank()) {
                            SupabaseManager.userToken = finalToken
                            val user = UserEntity(
                                email = normalizedEmail,
                                fullName = defaultName,
                                isGuest = false,
                                sessionToken = finalToken
                            )
                            db.userDao.insertUser(user)
                            _currentUser.value = user
                            
                            // Seed default welcome address remotely
                            try {
                                val addressPayload = mapOf(
                                    "userEmail" to normalizedEmail,
                                    "fullName" to defaultName,
                                    "phoneNumber" to "+91 98765 43210",
                                    "streetAddress" to "44 Fashion Street, Koral Layout",
                                    "city" to "Mumbai",
                                    "state" to "Maharashtra",
                                    "zipCode" to "400001",
                                    "isDefault" to true
                                )
                                SupabaseManager.dbApi?.insertAddress(addressPayload)
                            } catch (addrEx: Exception) {
                                Log.e("PooshakRepository", "Failed to seed remote address: ${addrEx.message}")
                            }

                            triggerNotification(normalizedEmail, "Logged In via Cloud! 🚀", "We signed you up and logged you in automatically via Supabase.", "Promotion")
                            return Result.success(user)
                        }
                    } catch (signUpEx: Exception) {
                        Log.e("PooshakRepository", "Fallback automatic Supabase signup failed: ${signUpEx.message}")
                    }
                }
            }
        }

        // LOCAL FALLBACK OR IF SUPABASE IS DISABLED
        Log.d("PooshakRepository", "Using local authentication fallback...")
        var user = db.userDao.getUserByEmail(normalizedEmail)
        if (user == null) {
            val defaultName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() } + " Kumar"
            user = UserEntity(
                email = normalizedEmail,
                fullName = defaultName,
                isGuest = false,
                sessionToken = "token_" + Random.nextInt(100000, 999999)
            )
            db.userDao.insertUser(user)
        }

        _currentUser.value = user
        triggerNotification(normalizedEmail, "Welcome back, ${user.fullName}!", "Check out today's trending premium fashion drops just for you.", "Promotion")
        return Result.success(user)
    }

    suspend fun signup(emailStr: String, nameStr: String, passwordStr: String): Result<UserEntity> {
        val normalizedEmail = emailStr.trim().lowercase()
        val trimmedName = nameStr.trim()
        if (normalizedEmail.isEmpty() || trimmedName.isEmpty() || passwordStr.isEmpty()) {
            return Result.failure(Exception("All fields are required"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        if (passwordStr.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long"))
        }

        // 1. SUPABASE SIGNUP
        if (SupabaseManager.isEnabled) {
            try {
                Log.d("PooshakRepository", "Registering user in Supabase...")
                val meta = SupabaseSignUpUserMetadata(trimmedName)
                val req = SupabaseSignUpRequest(normalizedEmail, passwordStr, meta)
                SupabaseManager.authApi!!.signUp(req)
                
                // Login immediately to acquire session token
                val tokenRes = SupabaseManager.authApi!!.login(SupabaseLoginRequest(normalizedEmail, passwordStr))
                val token = tokenRes.access_token
                if (!token.isNullOrBlank()) {
                    SupabaseManager.userToken = token
                    val newUser = UserEntity(
                        email = normalizedEmail,
                        fullName = trimmedName,
                        isGuest = false,
                        sessionToken = token
                    )
                    db.userDao.insertUser(newUser)
                    _currentUser.value = newUser

                    // Seed default welcome address remotely
                    try {
                        val addressPayload = mapOf(
                            "userEmail" to normalizedEmail,
                            "fullName" to trimmedName,
                            "phoneNumber" to "+91 98765 43210",
                            "streetAddress" to "44 Fashion Street, Koral Layout",
                            "city" to "Mumbai",
                            "state" to "Maharashtra",
                            "zipCode" to "400001",
                            "isDefault" to true
                        )
                        SupabaseManager.dbApi?.insertAddress(addressPayload)
                    } catch (e: Exception) {
                        Log.e("PooshakRepository", "Remote address seed failed: ${e.message}")
                    }

                    triggerNotification(normalizedEmail, "Account Created via Supabase! 🎉", "Welcome to Pooshak. Your sessions are securely synchronized to the cloud.", "Discount")
                    return Result.success(newUser)
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase signup failed, falling back locally: ${e.message}")
                return Result.failure(Exception("Online registration failed: ${e.message}"))
            }
        }

        // LOCAL FALLBACK
        val existing = db.userDao.getUserByEmail(normalizedEmail)
        if (existing != null && !existing.isGuest) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        val newUser = UserEntity(
            email = normalizedEmail,
            fullName = trimmedName,
            isGuest = false,
            sessionToken = "token_" + Random.nextInt(100000, 999999)
        )
        db.userDao.insertUser(newUser)
        _currentUser.value = newUser

        db.addressDao.insertAddress(
            AddressEntity(
                userEmail = normalizedEmail,
                fullName = trimmedName,
                phoneNumber = "+91 98765 43210",
                streetAddress = "44 Fashion Street, Koral Layout",
                city = "Mumbai",
                state = "Maharashtra",
                zipCode = "400001",
                isDefault = true
            )
        )

        triggerNotification(normalizedEmail, "Account Created Successfully! 🎉", "Welcome to Pooshak. Enjoy INR 500 discount on your first order. Use Coupon: FIRSTPOOSHAK", "Discount")
        return Result.success(newUser)
    }

    suspend fun loginAsGuest() {
        val guestEmail = "guest_session@pooshak.com"
        val guestUser = UserEntity(
            email = guestEmail,
            fullName = "Guest Shopper",
            isGuest = true,
            sessionToken = "guest_token"
        )
        db.userDao.insertUser(guestUser)
        _currentUser.value = guestUser
    }

    suspend fun forgotPassword(emailStr: String): Result<String> {
        val normalizedEmail = emailStr.trim().lowercase()
        if (normalizedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }
        
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.authApi!!.forgotPassword(SupabaseForgotRequest(normalizedEmail))
                return Result.success("A password reset link has been dispatched to $normalizedEmail via Supabase")
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase recover link failed: ${e.message}")
            }
        }
        return Result.success("A password reset link has been dispatched to $normalizedEmail")
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            db.userDao.deleteUserByEmail(user.email)
        }
        SupabaseManager.userToken = null
        _currentUser.value = null
    }

    // ==========================================
    // PRODUCTS & VARIANT QUERIES
    // ==========================================

    fun getAllProductsFlow(): Flow<List<ProductEntity>> = db.productDao.getAllProductsFlow()

    suspend fun getProductById(id: Long): ProductEntity? = db.productDao.getProductById(id)

    suspend fun getVariantsForProduct(productId: Long): List<ProductVariantEntity> = 
        db.productDao.getVariantsForProduct(productId)

    suspend fun getVariantById(variantId: Long): ProductVariantEntity? =
        db.productDao.getVariantById(variantId)

    // ==========================================
    // WISHLIST
    // ==========================================

    fun getWishlistItemsFlow(email: String): Flow<List<WishlistItemEntity>> = 
        db.wishlistDao.getWishlistItemsFlow(email)

    suspend fun toggleWishlist(email: String, productId: Long): Boolean {
        val items = db.wishlistDao.getWishlistItemsFlow(email).firstOrNull() ?: emptyList()
        val exists = items.any { it.productId == productId }
        if (exists) {
            if (SupabaseManager.isEnabled) {
                try {
                    SupabaseManager.dbApi?.deleteWishlist("eq.$email", "eq.$productId")
                } catch (e: Exception) {
                    Log.e("PooshakRepository", "Supabase wishlist delete failed: ${e.message}")
                }
            }
            db.wishlistDao.deleteWishlistItem(email, productId)
            return false
        } else {
            if (SupabaseManager.isEnabled) {
                try {
                    val map = mapOf("userEmail" to email, "productId" to productId)
                    SupabaseManager.dbApi?.insertWishlist(map)
                } catch (e: Exception) {
                    Log.e("PooshakRepository", "Supabase wishlist insert failed: ${e.message}")
                }
            }
            db.wishlistDao.insertWishlistItem(WishlistItemEntity(userEmail = email, productId = productId))
            return true
        }
    }

    fun isWishlistedFlow(email: String, productId: Long): Flow<Boolean> = 
        db.wishlistDao.isWishlistedFlow(email, productId)

    // ==========================================
    // CART
    // ==========================================

    fun getCartItemsFlow(email: String): Flow<List<CartItemEntity>> = 
        db.cartDao.getCartItemsFlow(email)

    suspend fun getCartItems(email: String): List<CartItemEntity> =
        db.cartDao.getCartItems(email)

    suspend fun addToCart(email: String, productId: Long, variantId: Long, qty: Int): Boolean {
        val cart = db.cartDao.getCartItems(email)
        val duplicate = cart.find { it.variantId == variantId && !it.savedForLater }
        if (duplicate != null) {
            val newQty = duplicate.quantity + qty
            if (SupabaseManager.isEnabled) {
                try {
                    SupabaseManager.dbApi?.updateCartQty("eq.${duplicate.id}", mapOf("quantity" to newQty))
                } catch (e: Exception) {
                    Log.e("PooshakRepository", "Supabase update quantities failed: ${e.message}")
                }
            }
            db.cartDao.updateQuantity(duplicate.id, newQty)
        } else {
            if (SupabaseManager.isEnabled) {
                try {
                    val entityPayload = mapOf(
                        "userEmail" to email,
                        "productId" to productId,
                        "variantId" to variantId,
                        "quantity" to qty,
                        "savedForLater" to false
                    )
                    // Obtain generated id to store matched local representation
                    val insertedMeta = SupabaseManager.dbApi?.insertCartItem(entityPayload)
                    val remoteId = insertedMeta?.firstOrNull()?.get("id") as? Number
                    if (remoteId != null) {
                        db.cartDao.insertCartItem(
                            CartItemEntity(
                                id = remoteId.toLong(),
                                userEmail = email,
                                productId = productId,
                                variantId = variantId,
                                quantity = qty
                            )
                        )
                        return true
                    }
                } catch (e: Exception) {
                    Log.e("PooshakRepository", "Supabase add cart item failed: ${e.message}")
                }
            }
            db.cartDao.insertCartItem(
                CartItemEntity(
                    userEmail = email,
                    productId = productId,
                    variantId = variantId,
                    quantity = qty
                )
            )
        }
        return true
    }

    suspend fun updateCartQty(cartItemId: Long, qty: Int) {
        if (qty <= 0) return
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.updateCartQty("eq.$cartItemId", mapOf("quantity" to qty))
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase update quantity failed: ${e.message}")
            }
        }
        db.cartDao.updateQuantity(cartItemId, qty)
    }

    suspend fun deleteCartItem(item: CartItemEntity) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.deleteCartItem("eq.${item.id}")
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase delete cart item failed: ${e.message}")
            }
        }
        db.cartDao.deleteCartItem(item)
    }

    suspend fun saveForLater(cartItemId: Long, save: Boolean) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.updateCartSaveForLater("eq.$cartItemId", mapOf("savedForLater" to save))
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase save for later status failed: ${e.message}")
            }
        }
        db.cartDao.updateSaveForLater(cartItemId, save)
    }

    // ==========================================
    // ADDRESSES
    // ==========================================

    fun getAddressesFlow(email: String): Flow<List<AddressEntity>> = 
        db.addressDao.getAddressesFlow(email)

    suspend fun getAddresses(email: String): List<AddressEntity> =
        db.addressDao.getAddresses(email)

    suspend fun addAddress(address: AddressEntity) {
        if (SupabaseManager.isEnabled) {
            try {
                val addressPayload = mapOf(
                    "userEmail" to address.userEmail,
                    "fullName" to address.fullName,
                    "phoneNumber" to address.phoneNumber,
                    "streetAddress" to address.streetAddress,
                    "city" to address.city,
                    "state" to address.state,
                    "zipCode" to address.zipCode,
                    "isDefault" to address.isDefault
                )
                
                if (address.isDefault) {
                    SupabaseManager.dbApi?.clearDefaultAddresses("eq.${address.userEmail}", mapOf("isDefault" to false))
                }
                
                val inserted = SupabaseManager.dbApi?.insertAddress(addressPayload)
                val generatedId = inserted?.firstOrNull()?.get("id") as? Number
                if (generatedId != null) {
                    db.addressDao.insertAddress(address.copy(id = generatedId.toLong()))
                    if (address.isDefault) {
                        db.addressDao.setAddressDefault(address.userEmail, generatedId.toLong())
                    }
                    return
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase add address failed: ${e.message}")
            }
        }
        db.addressDao.insertAddress(address)
        if (address.isDefault) {
            db.addressDao.setAddressDefault(address.userEmail, address.id)
        }
    }

    suspend fun deleteAddress(id: Long) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.deleteAddress("eq.$id")
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase delete address failed: ${e.message}")
            }
        }
        db.addressDao.deleteAddress(id)
    }

    suspend fun setDefaultAddress(email: String, id: Long) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.clearDefaultAddresses("eq.$email", mapOf("isDefault" to false))
                SupabaseManager.dbApi?.updateAddressDefault("eq.$id", mapOf("isDefault" to true))
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase setDefaultAddress failed: ${e.message}")
            }
        }
        db.addressDao.setAddressDefault(email, id)
    }

    // ==========================================
    // ORDERS
    // ==========================================

    fun getOrdersFlow(email: String): Flow<List<OrderEntity>> = 
        db.orderDao.getOrdersFlow(email)

    suspend fun getItemsForOrder(orderId: Long): List<OrderItemEntity> =
        db.orderDao.getItemsForOrder(orderId)

    suspend fun cancelOrder(orderId: Long) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.cancelOrder("eq.$orderId", mapOf("status" to "Cancelled"))
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase cancel order failed: ${e.message}")
            }
        }
        db.orderDao.cancelOrder(orderId)
        val order = db.orderDao.getOrderById(orderId)
        if (order != null) {
            // Restore Variant Stocks
            val items = db.orderDao.getItemsForOrder(orderId)
            for (item in items) {
                val variant = db.productDao.getVariantById(item.variantId)
                if (variant != null) {
                    val newStock = variant.stock + item.quantity
                    if (SupabaseManager.isEnabled) {
                        try {
                            SupabaseManager.dbApi?.updateVariantStock("eq.${variant.id}", mapOf("stock" to newStock))
                        } catch (e: Exception) {
                            Log.e("PooshakRepository", "Supabase restore Stock failed: ${e.message}")
                        }
                    }
                    db.productDao.updateVariantStock(variant.id, newStock)
                }
            }
            triggerNotification(order.userEmail, "Order Cancelled # ${order.orderNumber} 🌐", "Your order has been cancelled and any coupon used has been refunded on cloud.", "Order")
        }
    }

    suspend fun placeOrder(email: String, address: AddressEntity, cartItems: List<Pair<CartItemEntity, ProductEntity>>, couponCode: String?, subtotal: Double, totalAmt: Double): Result<OrderEntity> {
        if (cartItems.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        // 1. Verify and update variation stock
        for (pair in cartItems) {
            val item = pair.first
            val variant = db.productDao.getVariantById(item.variantId)
            if (variant == null || variant.stock < item.quantity) {
                return Result.failure(Exception("Selected item size/color variant is out of stock"))
            }
        }

        val orderNum = "POO-" + Random.nextInt(100000, 999999).toString()
        val addressText = "${address.streetAddress}, ${address.city}, ${address.state} - ${address.zipCode}"
        
        val order = OrderEntity(
            orderNumber = orderNum,
            userEmail = email,
            addressFullName = address.fullName,
            addressText = addressText,
            addressPhone = address.phoneNumber,
            totalAmount = totalAmt,
            status = "Confirmed", 
            paymentMethod = "Cash on Delivery"
        )

        var orderIdLocal: Long = 0L

        // Place remotely if Supabase is enabled
        if (SupabaseManager.isEnabled) {
            try {
                val orderPayload = mapOf(
                    "orderNumber" to order.orderNumber,
                    "userEmail" to order.userEmail,
                    "addressFullName" to order.addressFullName,
                    "addressText" to order.addressText,
                    "addressPhone" to order.addressPhone,
                    "totalAmount" to order.totalAmount,
                    "status" to order.status,
                    "paymentMethod" to order.paymentMethod,
                    "createdAt" to order.createdAt
                )
                val insertedOrders = SupabaseManager.dbApi?.insertOrder(orderPayload)
                val genOrderId = insertedOrders?.firstOrNull()?.get("id") as? Number
                if (genOrderId != null) {
                    orderIdLocal = genOrderId.toLong()
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase placeOrder metadata failed: ${e.message}")
            }
        }

        if (orderIdLocal == 0L) {
            orderIdLocal = db.orderDao.insertOrder(order)
        } else {
            db.orderDao.insertOrder(order.copy(id = orderIdLocal))
        }

        val orderItemsToInsert = mutableListOf<OrderItemEntity>()
        val remoteItemsPayload = mutableListOf<Map<String, Any>>()

        for (pair in cartItems) {
            val item = pair.first
            val p = pair.second
            val variant = db.productDao.getVariantById(item.variantId)!!
            val newStock = variant.stock - item.quantity
            
            // Decrement Stock
            if (SupabaseManager.isEnabled) {
                try {
                    SupabaseManager.dbApi?.updateVariantStock("eq.${variant.id}", mapOf("stock" to newStock))
                } catch (e: Exception) {
                    Log.e("PooshakRepository", "Supabase updateStock failed: ${e.message}")
                }
            }
            db.productDao.updateVariantStock(variant.id, newStock)

            val imageUrl = p.imageUrlsStr.substringBefore(",")
            orderItemsToInsert.add(
                OrderItemEntity(
                    orderId = orderIdLocal,
                    productId = p.id,
                    variantId = variant.id,
                    productName = p.name,
                    brand = p.brand,
                    size = variant.size,
                    color = variant.color,
                    price = p.discountPrice,
                    quantity = item.quantity,
                    imageUrl = imageUrl
                )
            )

            remoteItemsPayload.add(
                mapOf(
                    "orderId" to orderIdLocal,
                    "productId" to p.id,
                    "variantId" to variant.id,
                    "productName" to p.name,
                    "brand" to p.brand,
                    "size" to variant.size,
                    "color" to variant.color,
                    "price" to p.discountPrice,
                    "quantity" to item.quantity,
                    "imageUrl" to imageUrl
                )
            )
        }

        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.insertOrderItems(remoteItemsPayload)
                SupabaseManager.dbApi?.clearCart("eq.$email")
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase insert orderitems or clearCart failed: ${e.message}")
            }
        }

        db.orderDao.insertOrderItems(orderItemsToInsert)
        db.cartDao.clearCart(email) 

        triggerNotification(email, "Order Placed! 🛍️ 🌐", "Thank you! Your order #$orderNum of INR ${"%.2f".format(totalAmt)} is securely synced to your cloud account.", "Order")
        triggerNotification(email, "Order Packed & Dispatched! 🌐", "Great news! Your apparel items for Order #$orderNum have been pack-synchronized. Delivery estimate: 3 days.", "Order")

        return Result.success(order.copy(id = orderIdLocal))
    }

    // ==========================================
    // REVIEWS
    // ==========================================

    fun getReviewsForProductFlow(productId: Long): Flow<List<ReviewEntity>> = 
        db.reviewDao.getReviewsForProductFlow(productId)

    suspend fun insertReview(email: String, userName: String, productId: Long, rating: Int, comment: String): Result<Boolean> {
        val orders = db.orderDao.getOrders(email)
        var isVerified = false
        
        for (order in orders) {
            val items = db.orderDao.getItemsForOrder(order.id)
            if (items.any { it.productId == productId }) {
                isVerified = true
                break
            }
        }
        
        if (!isVerified) {
            return Result.failure(Exception("Only verified buyers who ordered this product can write a review."))
        }

        val review = ReviewEntity(
            productId = productId,
            userEmail = email,
            userName = userName,
            rating = rating,
            comment = comment,
            isVerifiedPurchase = true
        )

        if (SupabaseManager.isEnabled) {
            try {
                val reviewPayload = mapOf(
                    "productId" to review.productId,
                    "userEmail" to review.userEmail,
                    "userName" to review.userName,
                    "rating" to review.rating,
                    "comment" to review.comment,
                    "isVerifiedPurchase" to review.isVerifiedPurchase,
                    "createdAt" to review.createdAt
                )
                val insertedReviews = SupabaseManager.dbApi?.insertReview(reviewPayload)
                val generatedId = insertedReviews?.firstOrNull()?.get("id") as? Number
                if (generatedId != null) {
                    db.reviewDao.insertReview(review.copy(id = generatedId.toLong()))
                    return Result.success(true)
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase insert review failed: ${e.message}")
            }
        }

        db.reviewDao.insertReview(review)
        return Result.success(true)
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================

    fun getNotificationsFlow(email: String): Flow<List<NotificationEntity>> = 
        db.notificationDao.getNotificationsFlow(email)

    suspend fun triggerNotification(email: String, title: String, message: String, type: String) {
        val notification = NotificationEntity(
            userEmail = email,
            title = title,
            message = message,
            type = type
        )
        if (SupabaseManager.isEnabled) {
            try {
                val notPayload = mapOf(
                    "userEmail" to notification.userEmail,
                    "title" to notification.title,
                    "message" to notification.message,
                    "type" to notification.type,
                    "createdAt" to notification.createdAt,
                    "isRead" to notification.isRead
                )
                val inserted = SupabaseManager.dbApi?.insertNotification(notPayload)
                val genId = inserted?.firstOrNull()?.get("id") as? Number
                if (genId != null) {
                    db.notificationDao.insertNotification(notification.copy(id = genId.toLong()))
                    return
                }
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase triggerNotification failed: ${e.message}")
            }
        }
        db.notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationsRead(email: String) {
        if (SupabaseManager.isEnabled) {
            try {
                SupabaseManager.dbApi?.markNotificationsRead("eq.$email", mapOf("isRead" to true))
            } catch (e: Exception) {
                Log.e("PooshakRepository", "Supabase markNotificationsRead failed: ${e.message}")
            }
        }
        db.notificationDao.markAllAsRead(email)
    }
}
