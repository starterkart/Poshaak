package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. AUTHENTICATION & USER STATE
// ==========================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val isGuest: Boolean = false,
    val sessionToken: String? = null
)

// ==========================================
// 2. PRODUCT & VARIANT SYSTEM
// ==========================================

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val description: String,
    val fabric: String,
    val price: Double,
    val discountPrice: Double, // Price after discount, e.g. 1299 if MRP is 1999
    val rating: Float = 4.2f,
    val reviewsCount: Int = 0,
    val isPremium: Boolean = false,
    val isBestseller: Boolean = false,
    val isTrending: Boolean = false,
    val isBudget: Boolean = false,
    val gender: String, // "Men", "Women", "Kids", "Accessories"
    val category: String, // "T-Shirts", "Dresses", "Shirts", etc.
    val imageUrlsStr: String, // Comma separated list of preview image strings or localized asset descriptions
    val deliveryDays: Int = 3,
    val videoUrl: String? = null
)

@Entity(
    tableName = "product_variants",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val color: String,
    val size: String,
    val stock: Int
)

// ==========================================
// 3. CART & WISHLIST
// ==========================================

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val productId: Long,
    val variantId: Long, // References ProductVariantEntity
    val quantity: Int,
    val savedForLater: Boolean = false
)

@Entity(
    tableName = "wishlist_items",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val productId: Long
)

// ==========================================
// 4. ADDRESS & CHECKOUT
// ==========================================

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val fullName: String,
    val phoneNumber: String,
    val streetAddress: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val isDefault: Boolean = false
)

// ==========================================
// 5. ORDER & TRACKING SYSTEM
// ==========================================

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val userEmail: String,
    val addressFullName: String,
    val addressText: String,
    val addressPhone: String,
    val totalAmount: Double,
    val status: String, // "Pending", "Confirmed", "Packed", "Shipped", "Delivered", "Cancelled"
    val paymentMethod: String, // COD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val variantId: Long,
    val productName: String,
    val brand: String,
    val size: String,
    val color: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

// ==========================================
// 6. REVIEWS
// ==========================================

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userEmail: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val photoUrl: String? = null,
    val isVerifiedPurchase: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ==========================================
// 7. NOTIFICATIONS & PROMOTIONS
// ==========================================

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val title: String,
    val message: String,
    val type: String, // "Order", "Discount", "Sale", "Cart", "Promotion"
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// ==========================================
// 8. DATA ACCESS OBJECTS (DAOs)
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    suspend fun getVariantsForProduct(productId: Long): List<ProductVariantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Query("UPDATE product_variants SET stock = :newStock WHERE id = :variantId")
    suspend fun updateVariantStock(variantId: Long, newStock: Int)
    
    @Query("SELECT * FROM product_variants WHERE id = :variantId LIMIT 1")
    suspend fun getVariantById(variantId: Long): ProductVariantEntity?
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userEmail = :email")
    fun getCartItemsFlow(email: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userEmail = :email")
    suspend fun getCartItems(email: String): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE userEmail = :email")
    suspend fun clearCart(email: String)

    @Query("UPDATE cart_items SET quantity = :qty WHERE id = :id")
    suspend fun updateQuantity(id: Long, qty: Int)

    @Query("UPDATE cart_items SET savedForLater = :saved WHERE id = :id")
    suspend fun updateSaveForLater(id: Long, saved: Boolean)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userEmail = :email")
    fun getWishlistItemsFlow(email: String): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE userEmail = :email AND productId = :productId")
    suspend fun deleteWishlistItem(email: String, productId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE userEmail = :email AND productId = :productId)")
    fun isWishlistedFlow(email: String, productId: Long): Flow<Boolean>
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userEmail = :email")
    fun getAddressesFlow(email: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE userEmail = :email")
    suspend fun getAddresses(email: String): List<AddressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userEmail = :email")
    suspend fun clearDefaultFlags(email: String)

    @Transaction
    suspend fun setAddressDefault(email: String, addressId: Long) {
        clearDefaultFlags(email)
        querySetDefault(addressId)
    }

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun querySetDefault(id: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getOrdersFlow(email: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY createdAt DESC")
    suspend fun getOrders(email: String): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Long): List<OrderItemEntity>

    @Query("UPDATE orders SET status = 'Cancelled' WHERE id = :orderId")
    suspend fun cancelOrder(orderId: Long)

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProductFlow(productId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getNotificationsFlow(email: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email")
    suspend fun markAllAsRead(email: String)
}
