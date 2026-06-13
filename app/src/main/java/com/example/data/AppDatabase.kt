package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        ProductVariantEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ReviewEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val productDao: ProductDao
    abstract val cartDao: CartDao
    abstract val wishlistDao: WishlistDao
    abstract val addressDao: AddressDao
    abstract val orderDao: OrderDao
    abstract val reviewDao: ReviewDao
    abstract val notificationDao: NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pooshak_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
