package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseSync {
    private const val TAG = "SupabaseSync"

    // Helper to safely parse numeric values from Supabase JSON maps
    private fun Map<String, Any>.getLong(key: String): Long {
        val value = this[key] ?: return 0L
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun Map<String, Any>.getInt(key: String): Int {
        val value = this[key] ?: return 0
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun Map<String, Any>.getDouble(key: String): Double {
        val value = this[key] ?: return 0.0
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun Map<String, Any>.getFloat(key: String): Float {
        val value = this[key] ?: return 0.0f
        return when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0.0f
            else -> 0.0f
        }
    }

    private fun Map<String, Any>.getBooleanValue(key: String, default: Boolean = false): Boolean {
        val value = this[key] ?: return default
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            is Number -> value.toInt() != 0
            else -> default
        }
    }

    private fun Map<String, Any>.getString(key: String): String {
        return (this[key] ?: "").toString()
    }

    // ------------------------------------------
    // Products & Variants Synchronization
    // ------------------------------------------
    suspend fun syncProducts(db: AppDatabase) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            Log.d(TAG, "Checking products in Supabase...")
            val remoteProducts = dbApi.getProducts()
            
            if (remoteProducts.isEmpty()) {
                Log.d(TAG, "Supabase products list is empty. Triggering remote seeding...")
                // Seed local Room database first if needed
                ProductSeed.seedDatabase(db)
                val localProducts = db.productDao.getAllProducts()
                
                // Construct maps without local auto-generated 'id' to let Supabase autogenerate them,
                // OR upload as-is if keeping ID consistency is preferred.
                // To maintain perfect FK ties with variants, uploading mapped IDs works because Room IDs are incremental.
                val productsPayload = localProducts.map { p ->
                    mapOf(
                        "id" to p.id,
                        "name" to p.name,
                        "brand" to p.brand,
                        "description" to p.description,
                        "fabric" to p.fabric,
                        "price" to p.price,
                        "discountPrice" to p.discountPrice,
                        "rating" to p.rating,
                        "reviewsCount" to p.reviewsCount,
                        "isPremium" to p.isPremium,
                        "isBestseller" to p.isBestseller,
                        "isTrending" to p.isTrending,
                        "isBudget" to p.isBudget,
                        "gender" to p.gender,
                        "category" to p.category,
                        "imageUrlsStr" to p.imageUrlsStr,
                        "deliveryDays" to p.deliveryDays,
                        "videoUrl" to (p.videoUrl ?: "")
                    )
                }
                
                dbApi.insertProducts(productsPayload)
                Log.d(TAG, "Uploaded ${localProducts.size} products to Supabase.")

                // Upload variants
                val localVariants = mutableListOf<ProductVariantEntity>()
                for (p in localProducts) {
                    val variantsForProduct = db.productDao.getVariantsForProduct(p.id)
                    localVariants.addAll(variantsForProduct)
                }

                val variantsPayload = localVariants.map { v ->
                    mapOf(
                        "id" to v.id,
                        "productId" to v.productId,
                        "color" to v.color,
                        "size" to v.size,
                        "stock" to v.stock
                    )
                }
                dbApi.insertVariants(variantsPayload)
                Log.d(TAG, "Uploaded ${localVariants.size} product variants to Supabase.")
            } else {
                Log.d(TAG, "Found ${remoteProducts.size} products on Supabase. Downloading to Room...")
                
                // Map to entities
                val productEntities = remoteProducts.map { map ->
                    ProductEntity(
                        id = map.getLong("id"),
                        name = map.getString("name"),
                        brand = map.getString("brand"),
                        description = map.getString("description"),
                        fabric = map.getString("fabric"),
                        price = map.getDouble("price"),
                        discountPrice = map.getDouble("discountPrice"),
                        rating = map.getFloat("rating"),
                        reviewsCount = map.getInt("reviewsCount"),
                        isPremium = map.getBooleanValue("isPremium"),
                        isBestseller = map.getBooleanValue("isBestseller"),
                        isTrending = map.getBooleanValue("isTrending"),
                        isBudget = map.getBooleanValue("isBudget"),
                        gender = map.getString("gender"),
                        category = map.getString("category"),
                        imageUrlsStr = map.getString("imageUrlsStr"),
                        deliveryDays = map.getInt("deliveryDays"),
                        videoUrl = map.getString("videoUrl").takeIf { it.isNotBlank() }
                    )
                }
                
                db.productDao.insertProducts(productEntities)

                // Fetch and save variants
                val remoteVariants = dbApi.getVariants()
                val variantEntities = remoteVariants.map { map ->
                    ProductVariantEntity(
                        id = map.getLong("id"),
                        productId = map.getLong("productId"),
                        color = map.getString("color"),
                        size = map.getString("size"),
                        stock = map.getInt("stock")
                    )
                }
                db.productDao.insertVariants(variantEntities)
                Log.d(TAG, "Successfully synced ${productEntities.size} products and ${variantEntities.size} variants from Supabase to Room.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error synchronizing products/variants: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Cart Synchronization
    // ------------------------------------------
    suspend fun pullCart(db: AppDatabase, email: String) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteCart = dbApi.getCart("eq.$email")
            val cartEntities = remoteCart.map { map ->
                CartItemEntity(
                    id = map.getLong("id"),
                    userEmail = map.getString("userEmail"),
                    productId = map.getLong("productId"),
                    variantId = map.getLong("variantId"),
                    quantity = map.getInt("quantity"),
                    savedForLater = map.getBooleanValue("savedForLater")
                )
            }
            db.cartDao.clearCart(email)
            for (cart in cartEntities) {
                db.cartDao.insertCartItem(cart)
            }
            Log.d(TAG, "Synchronized ${cartEntities.size} cart items from Supabase.")
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling cart: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Wishlist Synchronization
    // ------------------------------------------
    suspend fun pullWishlist(db: AppDatabase, email: String) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteWishlist = dbApi.getWishlist("eq.$email")
            // Rebuild wishlist locally
            // First we can drop previous local items for that user to prevent duplicates
            db.wishlistDao.getWishlistItemsFlow(email) // we handle directly via dao or queries
            // Since we don't have clearWishlist in Dao, we can delete them matching user email manually
            // Or we insert directly. To avoid duplicates, let's just insert
            for (map in remoteWishlist) {
                val item = WishlistItemEntity(
                    id = map.getLong("id"),
                    userEmail = map.getString("userEmail"),
                    productId = map.getLong("productId")
                )
                db.wishlistDao.insertWishlistItem(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling wishlist: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Addresses Synchronization
    // ------------------------------------------
    suspend fun pullAddresses(db: AppDatabase, email: String) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteAddresses = dbApi.getAddresses("eq.$email")
            val entities = remoteAddresses.map { map ->
                AddressEntity(
                    id = map.getLong("id"),
                    userEmail = map.getString("userEmail"),
                    fullName = map.getString("fullName"),
                    phoneNumber = map.getString("phoneNumber"),
                    streetAddress = map.getString("streetAddress"),
                    city = map.getString("city"),
                    state = map.getString("state"),
                    zipCode = map.getString("zipCode"),
                    isDefault = map.getBooleanValue("isDefault")
                )
            }
            for (addr in entities) {
                db.addressDao.insertAddress(addr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling addresses: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Orders Synchronization
    // ------------------------------------------
    suspend fun pullOrders(db: AppDatabase, email: String) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteOrders = dbApi.getOrders("eq.$email")
            val orderEntities = remoteOrders.map { map ->
                OrderEntity(
                    id = map.getLong("id"),
                    orderNumber = map.getString("orderNumber"),
                    userEmail = map.getString("userEmail"),
                    addressFullName = map.getString("addressFullName"),
                    addressText = map.getString("addressText"),
                    addressPhone = map.getString("addressPhone"),
                    totalAmount = map.getDouble("totalAmount"),
                    status = map.getString("status"),
                    paymentMethod = map.getString("paymentMethod"),
                    createdAt = map.getLong("createdAt")
                )
            }

            for (order in orderEntities) {
                db.orderDao.insertOrder(order)
                
                // Pull Order Items for this order
                val remoteItems = dbApi.getOrderItems("eq.${order.id}")
                val itemEntities = remoteItems.map { map ->
                    OrderItemEntity(
                        id = map.getLong("id"),
                        orderId = map.getLong("orderId"),
                        productId = map.getLong("productId"),
                        variantId = map.getLong("variantId"),
                        productName = map.getString("productName"),
                        brand = map.getString("brand"),
                        size = map.getString("size"),
                        color = map.getString("color"),
                        price = map.getDouble("price"),
                        quantity = map.getInt("quantity"),
                        imageUrl = map.getString("imageUrl")
                    )
                }
                db.orderDao.insertOrderItems(itemEntities)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling orders: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Reviews Synchronization
    // ------------------------------------------
    suspend fun pullReviews(db: AppDatabase, productId: Long) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteReviews = dbApi.getReviews("eq.$productId")
            val entities = remoteReviews.map { map ->
                ReviewEntity(
                    id = map.getLong("id"),
                    productId = map.getLong("productId"),
                    userEmail = map.getString("userEmail"),
                    userName = map.getString("userName"),
                    rating = map.getInt("rating"),
                    comment = map.getString("comment"),
                    isVerifiedPurchase = map.getBooleanValue("isVerifiedPurchase"),
                    createdAt = map.getLong("createdAt")
                )
            }
            for (rev in entities) {
                db.reviewDao.insertReview(rev)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling reviews: ${e.message}", e)
        }
    }

    // ------------------------------------------
    // Notifications Synchronization
    // ------------------------------------------
    suspend fun pullNotifications(db: AppDatabase, email: String) = withContext(Dispatchers.IO) {
        val dbApi = SupabaseManager.dbApi ?: return@withContext
        try {
            val remoteNotifications = dbApi.getNotifications("eq.$email")
            val entities = remoteNotifications.map { map ->
                NotificationEntity(
                    id = map.getLong("id"),
                    userEmail = map.getString("userEmail"),
                    title = map.getString("title"),
                    message = map.getString("message"),
                    type = map.getString("type"),
                    createdAt = map.getLong("createdAt"),
                    isRead = map.getBooleanValue("isRead")
                )
            }
            for (n in entities) {
                db.notificationDao.insertNotification(n)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling notifications: ${e.message}", e)
        }
    }
}
