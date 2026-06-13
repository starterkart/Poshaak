package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Signup : Screen()
    object ForgotPassword : Screen()
    object Main : Screen() // Navigation tabs managed inside
    data class ProductDetails(val productId: Long) : Screen()
    object AdvancedSearch : Screen()
    object Checkout : Screen()
    data class OrderTracking(val orderId: Long) : Screen()
    object Notifications : Screen()
    object HelpCenter : Screen()
}

enum class MainTab {
    HOME, CATEGORIES, WISHLIST, CART, PROFILE
}

class PooshakViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = PooshakRepository(db)

    // ==========================================
    // NAVIGATION BACKSTACK
    // ==========================================
    val backStack = mutableStateListOf<Screen>(Screen.Splash)
    val currentTab = mutableStateOf(MainTab.HOME)

    fun navigateTo(screen: Screen) {
        // Prevent duplicate consecutive entries
        if (backStack.lastOrNull() != screen) {
            backStack.add(screen)
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            return true
        }
        return false // Exits app or no back action
    }

    // ==========================================
    // SYSTEM STATES
    // ==========================================
    val currentUser = repository.currentUser

    // Products
    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products = _products.asStateFlow()

    // Active Product Selection for detail page
    val activeProduct = mutableStateOf<ProductEntity?>(null)
    val activeVariants = mutableStateListOf<ProductVariantEntity>()
    val activeReviews = mutableStateListOf<ReviewEntity>()

    // Selected variations on details page
    val selectedSize = mutableStateOf("")
    val selectedColor = mutableStateOf("")
    val selectedQty = mutableStateOf(1)

    // Search and Filters
    val searchQuery = mutableStateOf("")
    val searchHistory = mutableStateListOf("Oversized", "Dresses", "Titanium", "Chikankari")
    val trendingSearches = listOf("Silk Saree", "Denim Jackets", "Oxford Shirts", "Midi Dress")
    
    // Filters State
    val filterPriceMax = mutableStateOf(10000f)
    val filterGender = mutableStateOf("") // "Men", "Women", "Kids", "Accessories"
    val filterCategory = mutableStateOf("")
    val filterSize = mutableStateOf("") // S, M, L, XL
    val filterColor = mutableStateOf("")
    val filterRating = mutableStateOf(0f)
    val filterDiscountOnly = mutableStateOf(false)
    val filterPremiumOnly = mutableStateOf(false)

    // Sorting State
    enum class SortType { POPULAR, TRENDING, NEW_ARRIVALS, PRICE_LOW_HIGH, PRICE_HIGH_LOW }
    val currentSort = mutableStateOf(SortType.POPULAR)

    // Cart and Checkout States
    val cartItems = mutableStateListOf<Pair<CartItemEntity, ProductEntity>>()
    val cartVariants = mutableMapOf<Long, ProductVariantEntity>() // Maps cartItemId -> Selected variant details
    val activeAddresses = mutableStateListOf<AddressEntity>()
    val selectedAddress = mutableStateOf<AddressEntity?>(null)
    
    // Coupon State
    val appliedCoupon = mutableStateOf<String?>(null)
    val couponStatusMessage = mutableStateOf("")
    
    // Wishlist
    val wishlistItems = mutableStateListOf<ProductEntity>()

    // Orders and reviews
    val ordersList = mutableStateListOf<OrderEntity>()
    val activeOrderTrack = mutableStateOf<OrderEntity?>(null)
    val activeOrderItems = mutableStateListOf<OrderItemEntity>()

    // Notifications
    val notificationsList = mutableStateListOf<NotificationEntity>()
    val unreadNotificationsCount = mutableStateOf(0)

    // Recently viewed list cache
    val recentlyViewed = mutableStateListOf<ProductEntity>()

    init {
        viewModelScope.launch {
            // Seeding database if empty on launch
            ProductSeed.seedDatabase(db)
            if (SupabaseManager.isEnabled) {
                try {
                    SupabaseSync.syncProducts(db)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            loadAllProducts()
            repository.initialSessionCheck()
            observeSessionAndCart()
        }
    }

    private fun observeSessionAndCart() {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                if (user != null && SupabaseManager.isEnabled) {
                    SupabaseManager.userToken = user.sessionToken
                    try {
                        SupabaseSync.pullCart(db, user.email)
                        SupabaseSync.pullWishlist(db, user.email)
                        SupabaseSync.pullAddresses(db, user.email)
                        SupabaseSync.pullOrders(db, user.email)
                        SupabaseSync.pullNotifications(db, user.email)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        var cartJob: kotlinx.coroutines.Job? = null
        var wishlistJob: kotlinx.coroutines.Job? = null
        var addressesJob: kotlinx.coroutines.Job? = null
        var ordersJob: kotlinx.coroutines.Job? = null
        var notificationsJob: kotlinx.coroutines.Job? = null

        viewModelScope.launch {
            repository.currentUser.collect { user ->
                cartJob?.cancel()
                wishlistJob?.cancel()
                addressesJob?.cancel()
                ordersJob?.cancel()
                notificationsJob?.cancel()

                if (user != null) {
                    // Observe cart
                    cartJob = launch {
                        repository.getCartItemsFlow(user.email).collect { items ->
                            cartItems.clear()
                            cartVariants.clear()
                            for (item in items) {
                                val p = repository.getProductById(item.productId)
                                if (p != null) {
                                    cartItems.add(item to p)
                                    val variants = repository.getVariantsForProduct(p.id)
                                    variants.find { it.id == item.variantId }?.let {
                                        cartVariants[item.id] = it
                                    }
                                }
                            }
                        }
                    }

                    // Observe wishlist
                    wishlistJob = launch {
                        repository.getWishlistItemsFlow(user.email).collect { wl ->
                            wishlistItems.clear()
                            for (item in wl) {
                                repository.getProductById(item.productId)?.let {
                                    wishlistItems.add(it)
                                }
                            }
                        }
                    }

                    // Observe Addresses
                    addressesJob = launch {
                        repository.getAddressesFlow(user.email).collect { addr ->
                            activeAddresses.clear()
                            activeAddresses.addAll(addr)
                            if (selectedAddress.value == null || !addr.contains(selectedAddress.value)) {
                                selectedAddress.value = addr.find { it.isDefault } ?: addr.firstOrNull()
                            }
                        }
                    }

                    // Observe Orders
                    ordersJob = launch {
                        repository.getOrdersFlow(user.email).collect { ords ->
                            ordersList.clear()
                            ordersList.addAll(ords)
                        }
                    }

                    // Observe Notifications
                    notificationsJob = launch {
                        repository.getNotificationsFlow(user.email).collect { list ->
                            notificationsList.clear()
                            notificationsList.addAll(list)
                            unreadNotificationsCount.value = list.count { !it.isRead }
                        }
                    }
                } else {
                    cartItems.clear()
                    cartVariants.clear()
                    wishlistItems.clear()
                    activeAddresses.clear()
                    selectedAddress.value = null
                    ordersList.clear()
                    notificationsList.clear()
                    unreadNotificationsCount.value = 0
                }
            }
        }
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            repository.getAllProductsFlow().collect { list ->
                _products.value = list
            }
        }
    }

    // ==========================================
    // AUTH ACTIONS
    // ==========================================

    fun triggerLogin(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.login(email, pass)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    onError(it.message ?: "Authentication Failed")
                }
        }
    }

    fun triggerSignup(email: String, name: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.signup(email, name, pass)
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    onError(it.message ?: "Signup Failed")
                }
        }
    }

    fun triggerGuestLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.loginAsGuest()
            onSuccess()
        }
    }

    fun triggerForgotPassword(email: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.forgotPassword(email)
                .onSuccess { onSuccess(it) }
                .onFailure { onError(it.message ?: "Password reset failed") }
        }
    }

    fun triggerLogout() {
        viewModelScope.launch {
            repository.logout()
            backStack.clear()
            backStack.add(Screen.Login)
        }
    }

    // ==========================================
    // RECENT VIEW TRACKER
    // ==========================================
    fun registerProductArrival(p: ProductEntity) {
        activeProduct.value = p
        selectedSize.value = ""
        selectedColor.value = ""
        selectedQty.value = 1

        // Add to recently viewed without duplicates
        if (recentlyViewed.contains(p)) {
            recentlyViewed.remove(p)
        }
        recentlyViewed.add(0, p)
        if (recentlyViewed.size > 10) {
            recentlyViewed.removeLast()
        }

        // Fetch variants and reviews
        viewModelScope.launch {
            val variants = repository.getVariantsForProduct(p.id)
            activeVariants.clear()
            activeVariants.addAll(variants)
            
            // Suggest first available size and color
            variants.firstOrNull { it.stock > 0 }?.let { v ->
                selectedSize.value = v.size
                selectedColor.value = v.color
            }

            // Fetch reviews
            if (SupabaseManager.isEnabled) {
                try {
                    SupabaseSync.pullReviews(db, p.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.getReviewsForProductFlow(p.id).collect { reviews ->
                activeReviews.clear()
                activeReviews.addAll(reviews)
            }
        }
    }

    // ==========================================
    // FILTER / SEARCH ENGINE
    // ==========================================
    fun executeSearch(query: String) {
        searchQuery.value = query
        if (query.isNotBlank() && !searchHistory.contains(query)) {
            searchHistory.add(0, query)
            if (searchHistory.size > 8) searchHistory.removeLast()
        }
        navigateTo(Screen.AdvancedSearch)
    }

    fun clearSearchAndFilters() {
        searchQuery.value = ""
        filterPriceMax.value = 10000f
        filterGender.value = ""
        filterCategory.value = ""
        filterSize.value = ""
        filterColor.value = ""
        filterRating.value = 0f
        filterDiscountOnly.value = false
        filterPremiumOnly.value = false
        currentSort.value = SortType.POPULAR
    }

    // Dynamic Filter Output
    fun getFilteredProducts(): List<ProductEntity> {
        var list = _products.value

        // 1. Search Query Matcher
        if (searchQuery.value.isNotBlank()) {
            val q = searchQuery.value.lowercase().trim()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.gender.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }

        // 2. Gender filtering
        if (filterGender.value.isNotBlank()) {
            list = list.filter { it.gender.equals(filterGender.value, ignoreCase = true) }
        }

        // 3. Category filtering
        if (filterCategory.value.isNotBlank()) {
            list = list.filter { it.category.equals(filterCategory.value, ignoreCase = true) }
        }

        // 4. Max Price
        list = list.filter { it.discountPrice <= filterPriceMax.value }

        // 5. Rating
        if (filterRating.value > 0) {
            list = list.filter { it.rating >= filterRating.value }
        }

        // 6. Discount Only
        if (filterDiscountOnly.value) {
            list = list.filter { it.discountPrice < it.price }
        }

        // 7. Premium Only
        if (filterPremiumOnly.value) {
            list = list.filter { it.isPremium }
        }

        // 8. Sorting
        return when (currentSort.value) {
            SortType.PRICE_LOW_HIGH -> list.sortedBy { it.discountPrice }
            SortType.PRICE_HIGH_LOW -> list.sortedByDescending { it.discountPrice }
            SortType.NEW_ARRIVALS -> list.sortedByDescending { it.id }
            SortType.TRENDING -> list.sortedByDescending { if (it.isTrending) 1 else 0 }
            SortType.POPULAR -> list.sortedByDescending { it.reviewsCount }
        }
    }

    // ==========================================
    // CART METHODS
    // ==========================================
    fun addToCartSelectedVariant(onCompleted: () -> Unit, onError: (String) -> Unit) {
        val user = currentUser.value
        val product = activeProduct.value
        if (user == null) {
            onError("Please log in to edit your shopping cart")
            navigateTo(Screen.Login)
            return
        }
        if (product == null) return

        if (selectedSize.value.isEmpty() || selectedColor.value.isEmpty()) {
            onError("Please select both size and color variants")
            return
        }

        // Match with specific variant id
        val match = activeVariants.find { it.size == selectedSize.value && it.color == selectedColor.value }
        if (match == null) {
            onError("The chosen size/color variation is unavailable")
            return
        }

        if (match.stock < selectedQty.value) {
            onError("Insufficient stock. Only ${match.stock} items left in stock")
            return
        }

        viewModelScope.launch {
            repository.addToCart(user.email, product.id, match.id, selectedQty.value)
            onCompleted()
        }
    }

    fun modifyCartVolume(cartItemId: Long, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQty(cartItemId, qty)
        }
    }

    fun removeCartProduct(item: CartItemEntity) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun toggleCartSaveLabel(cartItemId: Long, save: Boolean) {
        viewModelScope.launch {
            repository.saveForLater(cartItemId, save)
        }
    }

    fun applyPromoOffer(code: String) {
        val promo = code.uppercase().trim()
        if (promo == "FIRSTPOOSHAK") {
            appliedCoupon.value = "FIRSTPOOSHAK"
            couponStatusMessage.value = "FIFTY ₹500 off checkout applied!"
        } else if (promo == "FESTIVE20") {
            appliedCoupon.value = "FESTIVE20"
            couponStatusMessage.value = "20% off apparel total applied!"
        } else {
            appliedCoupon.value = null
            couponStatusMessage.value = "Invalid or expired promo code"
        }
    }

    // Cart calculations
    fun getCartSummary(): CartSummary {
        val activeItems = cartItems.filter { !it.first.savedForLater }
        var totalMRP = 0.0
        var totalDiscountPrice = 0.0
        
        for (pair in activeItems) {
            val item = pair.first
            val p = pair.second
            totalMRP += p.price * item.quantity
            totalDiscountPrice += p.discountPrice * item.quantity
        }

        val discountAmt = totalMRP - totalDiscountPrice
        var couponBenefit = 0.0

        if (appliedCoupon.value == "FIRSTPOOSHAK" && totalDiscountPrice >= 1200) {
            couponBenefit = 500.0
        } else if (appliedCoupon.value == "FESTIVE20") {
            couponBenefit = totalDiscountPrice * 0.2
        }

        val tempSub = totalDiscountPrice - couponBenefit
        val shipping = if (tempSub > 1500.0 || activeItems.isEmpty()) 0.0 else 149.0
        val grandTotal = if (activeItems.isEmpty()) 0.0 else (tempSub + shipping)

        return CartSummary(
            totalMRP = totalMRP,
            actualSale = totalDiscountPrice,
            productDiscount = discountAmt,
            couponDiscount = couponBenefit,
            shippingCharge = shipping,
            totalPayable = grandTotal
        )
    }

    data class CartSummary(
        val totalMRP: Double,
        val actualSale: Double,
        val productDiscount: Double,
        val couponDiscount: Double,
        val shippingCharge: Double,
        val totalPayable: Double
    )

    // ==========================================
    // WISHLIST TOGGLE
    // ==========================================
    fun toggleProductWishlist(productId: Long, onResult: (Boolean) -> Unit) {
        val user = currentUser.value
        if (user == null) {
            navigateTo(Screen.Login)
            return
        }
        viewModelScope.launch {
            val added = repository.toggleWishlist(user.email, productId)
            onResult(added)
        }
    }

    // ==========================================
    // ADDRESS
    // ==========================================
    fun addNewAddress(name: String, phone: String, street: String, city: String, stateStr: String, zip: String, default: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addAddress(
                AddressEntity(
                    userEmail = user.email,
                    fullName = name,
                    phoneNumber = phone,
                    streetAddress = street,
                    city = city,
                    state = stateStr,
                    zipCode = zip,
                    isDefault = default
                )
            )
        }
    }

    fun removeAddress(id: Long) {
        viewModelScope.launch {
            repository.deleteAddress(id)
        }
    }

    fun makeAddressPrimary(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.setDefaultAddress(user.email, id)
        }
    }

    // ==========================================
    // ORDER DISPATCH & TRACKING
    // ==========================================
    fun checkoutCurrentCart(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val user = currentUser.value ?: return
        val addr = selectedAddress.value
        val list = cartItems.filter { !it.first.savedForLater }

        if (addr == null) {
            onError("Please add or select a delivery address to complete order.")
            return
        }

        val summary = getCartSummary()

        viewModelScope.launch {
            repository.placeOrder(
                email = user.email,
                address = addr,
                cartItems = list,
                couponCode = appliedCoupon.value,
                subtotal = summary.actualSale,
                totalAmt = summary.totalPayable
            ).onSuccess { order ->
                appliedCoupon.value = null
                onSuccess(order.id)
            }.onFailure {
                onError(it.message ?: "Failed to place order")
            }
        }
    }

    fun loadOrderTracking(orderId: Long) {
        viewModelScope.launch {
            ordersList.find { it.id == orderId }?.let { order ->
                activeOrderTrack.value = order
                val items = repository.getItemsForOrder(orderId)
                activeOrderItems.clear()
                activeOrderItems.addAll(items)
                navigateTo(Screen.OrderTracking(orderId))
            }
        }
    }

    fun triggerOrderCancellation(orderId: Long) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
            // Refresh tracking
            val updated = db.orderDao.getOrderById(orderId)
            activeOrderTrack.value = updated
        }
    }

    // Advance order simulation
    fun simulateNextDeliveryState(orderId: Long) {
        viewModelScope.launch {
            val order = db.orderDao.getOrderById(orderId) ?: return@launch
            val nextStatus = when (order.status) {
                "Confirmed" -> "Packed"
                "Packed" -> "Shipped"
                "Shipped" -> "Delivered"
                else -> order.status
            }
            if (nextStatus != order.status) {
                val updated = order.copy(status = nextStatus)
                db.orderDao.insertOrder(updated)
                activeOrderTrack.value = updated
                
                // Trigger delivery and reviews authorization alert
                repository.triggerNotification(
                    email = order.userEmail,
                    title = "Order update: $nextStatus! 📦",
                    message = "Your apparel dispatch details updated for order #${order.orderNumber}.",
                    type = "Order"
                )
                if (nextStatus == "Delivered") {
                    repository.triggerNotification(
                        email = order.userEmail,
                        title = "Your package is here! 😍",
                        message = "Order #${order.orderNumber} successfully delivered. Leave a verified review on your newly acquired fashion pieces to help others shop!",
                        type = "Promotion"
                    )
                }

                // Refresh orders
                repository.getOrdersFlow(order.userEmail).firstOrNull()?.let { ords ->
                    ordersList.clear()
                    ordersList.addAll(ords)
                }
            }
        }
    }

    // ==========================================
    // REVIEWS WRITER (ONLY DELIVERED VERIFIED BUYER)
    // ==========================================
    fun submitVerifiedReview(productId: Long, rating: Int, comment: String, onResult: (Result<Boolean>) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.insertReview(
                email = user.email,
                userName = user.fullName,
                productId = productId,
                rating = rating,
                comment = comment
            )
            if (res.isSuccess) {
                // Refresh reviews
                repository.getReviewsForProductFlow(productId).firstOrNull()?.let { reviews ->
                    activeReviews.clear()
                    activeReviews.addAll(reviews)
                }
                
                // Update product stats
                val p = repository.getProductById(productId)
                if (p != null) {
                    val count = p.reviewsCount + 1
                    val newRating = ((p.rating * p.reviewsCount) + rating) / count
                    val updated = p.copy(reviewsCount = count, rating = newRating)
                    db.productDao.insertProducts(listOf(updated))
                    activeProduct.value = updated
                }
            }
            onResult(res)
        }
    }

    // ==========================================
    // READ NOTIFICATIONS
    // ==========================================
    fun clearAllNotificationsAsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markNotificationsRead(user.email)
        }
    }
}
