@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(viewModel: PooshakViewModel) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        kotlinx.coroutines.delay(2000)
        // Auto-navigate to login or main if session persists
        if (viewModel.currentUser.value != null) {
            viewModel.navigateTo(Screen.Main)
        } else {
            viewModel.navigateTo(Screen.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CharcoalDark, Color(0xFF1E2024))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant glowing logo container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, GoldSecondary.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Customized Brand letter P
                Text(
                    text = "P",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "P O O S H A K",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldSecondary,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "PREMIUM COUTURES & EVERYDAY STYLES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MutedGrey,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "INPIRED BY MILAN & PARIS",
            fontSize = 9.sp,
            fontWeight = FontWeight.Normal,
            color = GoldSecondary.copy(alpha = 0.4f),
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}

// ==========================================
// 2. AUTHENTICATION SCREENS
// ==========================================
@Composable
fun LoginScreen(viewModel: PooshakViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Welcome to Pooshak",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Drape yourself in absolute luxury",
                fontSize = 12.sp,
                color = MutedGrey,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMsg = "" },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = "" },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = CrimsonAlert,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Forgot Password Link
            TextButton(
                onClick = { viewModel.navigateTo(Screen.ForgotPassword) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = GoldPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    viewModel.triggerLogin(
                        email = email,
                        pass = password,
                        onSuccess = {
                            viewModel.navigateTo(Screen.Main)
                        },
                        onError = { errorMsg = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LOG IN", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest Browsing button
            OutlinedButton(
                onClick = {
                    viewModel.triggerGuestLogin {
                        viewModel.navigateTo(Screen.Main)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("guest_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Text("BROWSE AS GUEST", fontWeight = FontWeight.Bold, color = GoldPrimary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New to Pooshak?", color = MutedGrey, fontSize = 14.sp)
                TextButton(onClick = { viewModel.navigateTo(Screen.Signup) }) {
                    Text("Create Account", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SignupScreen(viewModel: PooshakViewModel) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Unlock personalized high-street drops",
                fontSize = 12.sp,
                color = MutedGrey,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = "" },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMsg = "" },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_signup_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = "" },
                label = { Text("Password (6+ Characters)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_signup_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = CrimsonAlert,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.triggerSignup(
                        email = email,
                        name = name,
                        pass = password,
                        onSuccess = {
                            viewModel.navigateTo(Screen.Main)
                        },
                        onError = { errorMsg = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("signup_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SIGN UP", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already registered?", color = MutedGrey, fontSize = 14.sp)
                TextButton(onClick = { viewModel.navigateTo(Screen.Login) }) {
                    Text("Log In", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: PooshakViewModel) {
    var email by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter your email to receive recovery instructions",
                fontSize = 12.sp,
                color = MutedGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMsg = ""; successMsg = "" },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = CrimsonAlert,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (successMsg.isNotEmpty()) {
                Text(
                    text = successMsg,
                    color = GreenSuccess,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.triggerForgotPassword(
                        email = email,
                        onSuccess = { successMsg = it },
                        onError = { errorMsg = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DISPATCH LINK", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.navigateTo(Screen.Login) }) {
                Text("Return to Login", color = GoldPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 3. MAIN NAVIGATION TABS WRAPPERS
// ==========================================

@Composable
fun TabHomeScreen(viewModel: PooshakViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle(initialValue = emptyList())
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wishlistItems = viewModel.wishlistItems

    // Pre-calculate lists filtered by tags for fast smooth loads
    val premiumList = remember(products) { products.filter { it.isPremium } }
    val bestsellers = remember(products) { products.filter { it.isBestseller } }
    val trendings = remember(products) { products.filter { it.isTrending } }
    val budgetPicks = remember(products) { products.filter { it.discountPrice < 1500 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Gold Luxury Headers with Quick Search bar trigger
        SearchBarDummy(onClick = { viewModel.navigateTo(Screen.AdvancedSearch) })

        // HERO BANNER CAROUSEL
        HeroBannerSection(
            onBannerClick = { category, gender ->
                viewModel.clearSearchAndFilters()
                viewModel.filterCategory.value = category
                viewModel.filterGender.value = gender
                viewModel.navigateTo(Screen.AdvancedSearch)
            }
        )

        // CATEGORY ROUNDELS
        CategoryRoundelsSection(
            onCategoryClick = { gender ->
                viewModel.clearSearchAndFilters()
                viewModel.filterGender.value = gender
                viewModel.currentTab.value = MainTab.CATEGORIES
            }
        )

        // FLASH SALE COUNTDOWN WITH LIVE TIMER
        FlashSaleRowSlider(
            products = trendings.take(4),
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // TRENDING NOW
        HorizontalDividerContainer(title = "Trending Now")
        HorizontalProductsScrollRow(
            products = trendings,
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // NEW ARRIVALS
        HorizontalDividerContainer(title = "New Arrivals")
        HorizontalProductsScrollRow(
            products = products.reversed().take(6),
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // BEST SELLERS
        HorizontalDividerContainer(title = "Best Sellers")
        HorizontalProductsScrollRow(
            products = bestsellers,
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // BUDGET PICKS
        HorizontalDividerContainer(title = "Budget Friendly Cuts (< ₹1500)")
        HorizontalProductsScrollRow(
            products = budgetPicks,
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // PREMIUM COLLECTION COUTURE
        HorizontalDividerContainer(title = "Meticulous Couture (Premium)")
        HorizontalProductsScrollRow(
            products = premiumList,
            wishlist = wishlistItems,
            onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
            onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
        )

        // RECENTLY VIEWED (If loaded)
        if (viewModel.recentlyViewed.isNotEmpty()) {
            HorizontalDividerContainer(title = "Recently Viewed")
            HorizontalProductsScrollRow(
                products = viewModel.recentlyViewed,
                wishlist = wishlistItems,
                onProductClick = { viewModel.registerProductArrival(it); viewModel.navigateTo(Screen.ProductDetails(it.id)) },
                onWishlistToggle = { viewModel.toggleProductWishlist(it.id) { _ -> } }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TabCategoriesScreen(viewModel: PooshakViewModel) {
    val gendersList = listOf("Men", "Women", "Kids", "Accessories")
    val currentSelection = remember { mutableStateOf("Men") }

    val products by viewModel.products.collectAsStateWithLifecycle(initialValue = emptyList())
    val wishlistItems = viewModel.wishlistItems

    // Subcategories Matrix
    val subcategories = remember(currentSelection.value) {
        when (currentSelection.value) {
            "Men" -> listOf("All Menswear", "T-Shirts", "Shirts", "Hoodies", "Jackets", "Jeans", "Pants")
            "Women" -> listOf("All Womenswear", "Dresses", "Kurtis", "Sarees", "Tops")
            "Kids" -> listOf("All Kidswear", "Boys clothing", "Girls clothing", "Shoes")
            "Accessories" -> listOf("All Accents", "Watches", "Sunglasses", "Wallets")
            else -> emptyList()
        }
    }

    val selectedSub = remember { mutableStateOf("All Menswear") }
    LaunchedEffect(currentSelection.value) {
        selectedSub.value = "All " + currentSelection.value + (if (currentSelection.value == "Accessories") " Accents" else "wear")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Primary Gender Hub Bar
        TabRow(
            selectedTabIndex = gendersList.indexOf(currentSelection.value),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[gendersList.indexOf(currentSelection.value)]),
                    color = GoldPrimary
                )
            }
        ) {
            gendersList.forEach { gender ->
                Tab(
                    selected = currentSelection.value == gender,
                    onClick = { currentSelection.value = gender },
                    text = { Text(gender) }
                )
            }
        }

        // Horizontal Category Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subcategories) { sub ->
                val isActive = selectedSub.value == sub
                FilterChip(
                    selected = isActive,
                    onClick = { selectedSub.value = sub },
                    label = { Text(sub, color = if (isActive) Color.White else MaterialTheme.colorScheme.onBackground) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary
                    )
                )
            }
        }

        // Show Matching Products Grid
        val matchedProducts = remember(currentSelection.value, selectedSub.value, products) {
            products.filter { p ->
                val isGenderMatch = p.gender.equals(currentSelection.value, ignoreCase = true)
                val isSubMatch = if (selectedSub.value.startsWith("All")) {
                    true
                } else {
                    p.category.equals(selectedSub.value, ignoreCase = true)
                }
                isGenderMatch && isSubMatch
            }
        }

        if (matchedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(48.dp))
                    Text("No creations in this closet yet", modifier = Modifier.padding(top = 8.dp), color = MutedGrey)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(matchedProducts) { p ->
                    FashionProductCard(
                        product = p,
                        isWishlisted = wishlistItems.any { it.id == p.id },
                        onProductClick = {
                            viewModel.registerProductArrival(p)
                            viewModel.navigateTo(Screen.ProductDetails(p.id))
                        },
                        onWishlistClick = {
                            viewModel.toggleProductWishlist(p.id) { _ -> }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TabWishlistScreen(viewModel: PooshakViewModel) {
    val wishlist = viewModel.wishlistItems

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Your Wishlist (${wishlist.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (wishlist.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "",
                        tint = MutedGrey,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Wardrobe Wishlist is Empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Touch the floating heart icon on fashion products to collection items here.",
                        fontSize = 12.sp,
                        color = MutedGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.currentTab.value = MainTab.HOME },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("START SHOPPING", color = Color.White)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wishlist) { p ->
                    FashionProductCard(
                        product = p,
                        isWishlisted = true,
                        onProductClick = {
                            viewModel.registerProductArrival(p)
                            viewModel.navigateTo(Screen.ProductDetails(p.id))
                        },
                        onWishlistClick = {
                            viewModel.toggleProductWishlist(p.id) { _ -> }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TabCartScreen(viewModel: PooshakViewModel) {
    val cart = viewModel.cartItems
    val summary = viewModel.getCartSummary()
    var promoInput by remember { mutableStateOf("") }

    val activeItems = cart.filter { !it.first.savedForLater }
    val savedItems = cart.filter { it.first.savedForLater }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Shopping Bag (${activeItems.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (activeItems.isEmpty() && savedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Outlined.ShoppingBag, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(72.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Your bag is feather light!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("There are no apparel choices in your bag currently", color = MutedGrey, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            // BAG ITEMS
            items(activeItems) { pair ->
                val item = pair.first
                val p = pair.second
                val variant = viewModel.cartVariants[item.id]

                CartLineItemRow(
                    product = p,
                    item = item,
                    variant = variant,
                    onQtyChange = { viewModel.modifyCartVolume(item.id, it) },
                    onDelete = { viewModel.removeCartProduct(item) },
                    onToggleSave = { viewModel.toggleCartSaveLabel(item.id, true) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // PROMO / COUPONS PANEL
            if (activeItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Apply Coupon Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = { promoInput = it },
                                    placeholder = { Text("FIRSTPOOSHAK / FESTIVE20") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("coupon_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.applyPromoOffer(promoInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Apply", color = Color.White)
                                }
                            }
                            if (viewModel.couponStatusMessage.value.isNotEmpty()) {
                                Text(
                                    text = viewModel.couponStatusMessage.value,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (viewModel.appliedCoupon.value != null) GreenSuccess else CrimsonAlert,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                // BILL DESCRIPTION
                item {
                    BillingSummaryCard(summary = summary)
                }
            }

            // SAVE FOR LATER SECTION
            if (savedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved for Later (${savedItems.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoldPrimary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                items(savedItems) { pair ->
                    val item = pair.first
                    val p = pair.second
                    val variant = viewModel.cartVariants[item.id]

                    CartLineItemRow(
                        product = p,
                        item = item,
                        variant = variant,
                        onQtyChange = { },
                        onDelete = { viewModel.removeCartProduct(item) },
                        onToggleSave = { viewModel.toggleCartSaveLabel(item.id, false) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // CHECKOUT DISPATCH ACTION PILL
        if (activeItems.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Grand Total", fontSize = 12.sp, color = MutedGrey)
                        Text(
                            "₹${summary.totalPayable.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.Checkout) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("checkout_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PROCEED TO CHECKOUT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TabProfileScreen(viewModel: PooshakViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showAddressModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Elite profile cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName ?: "G").take(1).uppercase(),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.fullName ?: "Guest Browser",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user?.email ?: "guest_browse@pooshak.com",
                            fontSize = 12.sp,
                            color = MutedGrey
                        )
                    }
                }
            }
        }

        // Supabase Status Badge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (com.example.data.SupabaseManager.isEnabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = if (com.example.data.SupabaseManager.isEnabled) GoldPrimary else MutedGrey.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (com.example.data.SupabaseManager.isEnabled) GreenSuccess else GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (com.example.data.SupabaseManager.isEnabled) "SUPABASE BACKEND ONLINE" else "SUPABASE BACKEND: LOCAL SANDBOX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (com.example.data.SupabaseManager.isEnabled) GreenSuccess else GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (com.example.data.SupabaseManager.isEnabled) {
                                "Direct Postgres database synchronized. Orders, comments, & wishlist backed up in cloud."
                            } else {
                                "Running in local database sandbox mode. Connect to Supabase to enable cloud persistence."
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Standard links blocks
        item {
            Text(
                text = "Shopping Wardrobes",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }

        item {
            ProfileMenuOption(
                icon = Icons.Default.ShoppingCart,
                title = "Order History & Status",
                subtitle = "Manage tracking updates & cancel orders",
                onClick = {
                    // Show historical list by switching or filtering
                }
            )
        }

        // Dynamic Orders list right inside profile for excellent fluid tracking
        if (viewModel.ordersList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "No order history yet.",
                        modifier = Modifier.padding(16.dp),
                        color = MutedGrey,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(viewModel.ordersList) { ord ->
                OrderHistoryRow(
                    order = ord,
                    onTrackClick = { viewModel.loadOrderTracking(ord.id) }
                )
            }
        }

        item {
            ProfileMenuOption(
                icon = Icons.Default.Place,
                title = "Saved Addresses",
                subtitle = "Add, remove & edit default delivery layout",
                onClick = { showAddressModal = true }
            )
        }

        // Dynamic Addresses list right inside profile
        items(viewModel.activeAddresses) { addr ->
            AddressItemRow(
                address = addr,
                onDelete = { viewModel.removeAddress(addr.id) },
                onSetDefault = { viewModel.makeAddressPrimary(addr.id) }
            )
        }

        item {
            Text(
                text = "System Settings",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }

        item {
            ProfileMenuOption(
                icon = Icons.Default.Notifications,
                title = "Notification Panel",
                subtitle = "Sales, order tracks & coupons",
                badgeCount = viewModel.unreadNotificationsCount.value,
                onClick = { viewModel.navigateTo(Screen.Notifications) }
            )
        }

        item {
            ProfileMenuOption(
                icon = Icons.Default.Info,
                title = "Help & Live Support",
                subtitle = "Contact coutures center & FAQs",
                onClick = { viewModel.navigateTo(Screen.HelpCenter) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.triggerLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, CrimsonAlert),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LOG OUT SESSION", color = CrimsonAlert, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Modal adding drawer if true
    if (showAddressModal) {
        DialogAddAddress(
            onDismiss = { showAddressModal = false },
            onSave = { name, phone, str, c, st, z, d ->
                viewModel.addNewAddress(name, phone, str, c, st, z, d)
                showAddressModal = false
            }
        )
    }
}

// ==========================================
// 4. ADVANCED SEARCH & FILTERING SCREEN
// ==========================================
@Composable
fun AdvancedSearchScreen(viewModel: PooshakViewModel) {
    var rawInput by remember { mutableStateOf(viewModel.searchQuery.value) }
    val results = viewModel.getFilteredProducts()
    val wishlistItems = viewModel.wishlistItems

    var showFilterDrawer by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.searchQuery.value) {
        rawInput = viewModel.searchQuery.value
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 4.dp) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                        }

                        OutlinedTextField(
                            value = rawInput,
                            onValueChange = { rawInput = it },
                            placeholder = { Text("Search Zara, cotton wear, tops...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (rawInput.isNotEmpty()) {
                                    IconButton(onClick = { rawInput = ""; viewModel.searchQuery.value = "" }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { viewModel.executeSearch(rawInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("GO", color = Color.White)
                        }
                    }

                    // Direct Quick Sort & Option Triggers bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showFilterDrawer = true },
                            modifier = Modifier
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        }

                        // Sorting quick chips
                        PooshakViewModel.SortType.values().forEach { sort ->
                            val isSelected = viewModel.currentSort.value == sort
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.currentSort.value = sort },
                                label = { Text(sort.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // If query blank and no active search -> show suggestions
            if (rawInput.isBlank() && results.size == viewModel.products.value.size) {
                // SUGGESTIONS LISTS
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trending Searches", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldPrimary)
                    FlowRow(
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        viewModel.trendingSearches.forEach { tr ->
                            SuggestionChipItem(text = tr, onClick = { rawInput = tr; viewModel.executeSearch(tr) })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recent History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldPrimary)
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        viewModel.searchHistory.forEach { hist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rawInput = hist; viewModel.executeSearch(hist) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(hist, fontSize = 14.sp)
                                }
                                Icon(imageVector = Icons.Default.ArrowOutward, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            } else {
                // Match search results
                if (results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(72.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No fashion matches found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Try clarifying query words or resetting active filters", color = MutedGrey, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearSearchAndFilters(); rawInput = "" }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                                Text("CLEAR FILTERS", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results) { p ->
                            FashionProductCard(
                                product = p,
                                isWishlisted = wishlistItems.any { it.id == p.id },
                                onProductClick = {
                                    viewModel.registerProductArrival(p)
                                    viewModel.navigateTo(Screen.ProductDetails(p.id))
                                },
                                onWishlistClick = {
                                    viewModel.toggleProductWishlist(p.id) { _ -> }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Filters Bottom drawer
    if (showFilterDrawer) {
        DialogAdvancedFilters(
            viewModel = viewModel,
            onDismiss = { showFilterDrawer = false }
        )
    }
}

// ==========================================
// 5. CHECKOUT SCREEN
// ==========================================
@Composable
fun CheckoutScreen(viewModel: PooshakViewModel) {
    val items = viewModel.cartItems.filter { !it.first.savedForLater }
    val summary = viewModel.getCartSummary()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Checkout Address & Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // STEP CARD Address choice
            Text("Select Shipping Address", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.activeAddresses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(Screen.Main); viewModel.currentTab.value = MainTab.PROFILE },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("+ Add address in profile menu options", modifier = Modifier.padding(24.dp), color = GoldPrimary, textAlign = TextAlign.Center)
                }
            } else {
                viewModel.activeAddresses.forEach { addr ->
                    val isChosen = viewModel.selectedAddress.value?.id == addr.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(if (isChosen) 1.5.dp else 0.dp, if (isChosen) GoldPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectedAddress.value = addr },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isChosen,
                                onClick = { viewModel.selectedAddress.value = addr },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(addr.fullName, fontWeight = FontWeight.Bold)
                                Text("${addr.streetAddress}, ${addr.city}, ${addr.state} - ${addr.zipCode}", fontSize = 12.sp, color = MutedGrey)
                                Text("Call: ${addr.phoneNumber}", fontSize = 11.sp, color = MutedGrey)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PAYMENT METHODS PANEL
            Text("Choose Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // COD Working
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = true, onClick = { }, colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Cash on Delivery (CoD)", fontWeight = FontWeight.Bold)
                            Text("Order securely. Pay only when bag is delivered.", fontSize = 11.sp, color = GreenSuccess)
                        }
                    }
                }

                // Disabled cards list (Myntra layout)
                listOf("UPI / GooglePay (Unavailable)", "Visa/Mastercard Credit Card (Coming Soon)", "Net Banking / Wallets (Coming Soon)").forEach { pay ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = false, enabled = false, onClick = { })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(pay, color = MutedGrey, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PURCHASE BILL DETAILS
            BillingSummaryCard(summary = summary)

            Spacer(modifier = Modifier.height(40.dp))

            // SUBMIT ORDER BUTTON
            Button(
                onClick = {
                    viewModel.checkoutCurrentCart(
                        onSuccess = { orderId ->
                            viewModel.navigateTo(Screen.OrderTracking(orderId))
                            viewModel.loadOrderTracking(orderId)
                        },
                        onError = {
                            // Show generic snack bar or feedback
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_order_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PLACE TRUSTED CoD ORDER (₹${summary.totalPayable.toInt()})", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ==========================================
// 6. ORDER TRACKING SCREEN
// ==========================================
@Composable
fun OrderTrackingScreen(viewModel: PooshakViewModel) {
    val order = viewModel.activeOrderTrack.value
    val items = viewModel.activeOrderItems

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Receipt & Live Tracker", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Main); viewModel.currentTab.value = MainTab.PROFILE }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // STATUS ALERT
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Order Number: #${order.orderNumber}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Text(
                            text = "Status: ${order.status.uppercase()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (order.status == "Cancelled") CrimsonAlert else GreenSuccess,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // INJECTED FOR DEMONSTRATING DELIVERIES: Simulator button
                        if (order.status != "Cancelled" && order.status != "Delivered") {
                            OutlinedButton(
                                onClick = { viewModel.simulateNextDeliveryState(order.id) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GoldPrimary)
                            ) {
                                Text("SIMULATE NEXT TRANSIT STEP 📦", fontSize = 11.sp, color = GoldPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TIMELINE VISUAL STAGES
                Text("Transit Timeline Progress", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                val orderedStages = listOf("Confirmed", "Packed", "Shipped", "Delivered")
                val activeIndex = orderedStages.indexOf(order.status)

                if (order.status == "Cancelled") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = "", tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("This order has been successfully cancelled & items returned to inventory.", color = CrimsonAlert)
                    }
                } else {
                    Column {
                        orderedStages.forEachIndexed { idx, stage ->
                            val isCompleted = idx <= activeIndex
                            TimelineNodeItem(
                                title = stage,
                                subtitle = getTimelineStageMessage(stage),
                                isCompleted = isCompleted,
                                isLast = idx == orderedStages.size - 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ORDER ITEMS CARD LIST
                Text("Clothing Selections (${items.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            // Virtual square graphic item colors reflecting category
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getFashionGradient(item.color + item.size, item.productId)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.productName.take(1), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.brand.uppercase(), fontSize = 10.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text(item.productName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Size: ${item.size}  |  Color: ${item.color}  |  Qty: ${item.quantity}", fontSize = 11.sp, color = MutedGrey)
                            }
                            Text("₹${(item.price * item.quantity).toInt()}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DELIVERY DETAILS ADDRESS PANEL
                Text("Deliver To", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(order.addressFullName, fontWeight = FontWeight.Bold)
                        Text(order.addressText, color = MutedGrey, fontSize = 12.sp)
                        Text("Phone Contact: ${order.addressPhone}", color = MutedGrey, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BILL CASH DETAILS
                BillingTotalSection(totalAmount = order.totalAmount)

                // ACTIONS BUTTONS
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Invoice Download Button
                    Button(
                        onClick = {
                            // Dummy PDF download trigger
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("INVOICE.PDF", color = Color.White)
                    }

                    // Cancellation
                    if (order.status != "Cancelled" && order.status != "Delivered") {
                        Button(
                            onClick = { viewModel.triggerOrderCancellation(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, CrimsonAlert),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CANCEL BAG", color = CrimsonAlert, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. NOTIFICATIONS PANEL
// ==========================================
@Composable
fun NotificationsScreen(viewModel: PooshakViewModel) {
    val list = viewModel.notificationsList

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Messaages & Promo Alerts", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearAllNotificationsAsRead() }) {
                        Text("Mark all read", color = GoldPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(72.dp))
                        Text("You're all caught up", modifier = Modifier.padding(top = 12.dp), color = MutedGrey)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(list) { alert ->
                        val alertColor = when (alert.type) {
                            "Order" -> GreenSuccess.copy(alpha = 0.08f)
                            "Discount" -> GoldSecondary.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = alertColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (!alert.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(alert.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. HELP & FAQs SCREEN
// ==========================================
@Composable
fun HelpCenterScreen(viewModel: PooshakViewModel) {
    val faqsList = listOf(
        "Is shipping completely free?" to "Yes, we dispatch order parcels absolutely free on checkout shopping values exceeding INR 1500. Otherwise, a standard charge of INR 149 is applied.",
        "How can I return fashion items?" to "You can easily trigger a return or replacement ticket on any items by emailing care@pooshak.com alongside a download copy of your invoice within 14 days of delivery receipt.",
        "How do I track deliveries?" to "Every confirmed Order details tab in your Profile page features a simulated status updater that mirrors real carriage logistics.",
        "Are sizes standardized?" to "Every product page details page houses an exact size chart (S, M, L, XL) mapping body metrics cleanly."
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help Center & FAQ", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Need Immediate Styling Advice?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Speak with our curated Paris & Milan fashion concierges.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CALL CONCIERGE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldPrimary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(faqsList) { item ->
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "")
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.second, fontSize = 12.sp, color = MutedGrey)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// LOWER HELPER DUMMY COMPOSABLES & LAYOUTS
// ==========================================

@Composable
fun SearchBarDummy(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customized letter P as beautiful small brand ring
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("P", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "", tint = MutedGrey)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search threads, kurtis, silk sarees...", color = MutedGrey, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBannerSection(onBannerClick: (String, String) -> Unit) {
    // Elegant luxury promotion banner
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .clickable { onBannerClick("Dresses", "Women") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalMuted)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CharcoalDark, Color(0xFF33202E))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(220.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = GoldSecondary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("ZARA STUDIO DROP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CharcoalDark, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Midnight Silk Collection", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 26.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Feminine slips & layered midi robes", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, GoldSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("ZARA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CategoryRoundelsSection(onCategoryClick: (String) -> Unit) {
    val roundels = listOf("Men", "Women", "Kids", "Accessories")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(roundels) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCategoryClick(category) }
                    .padding(horizontal = 12.dp)
            ) {
                val accentColor = when (category) {
                    "Women" -> Color(0xFFC49CA4)
                    "Men" -> Color(0xFF9CAAC4)
                    "Kids" -> Color(0xFF9CC4A9)
                    else -> GoldSecondary
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when(category) {
                        "Men" -> Icons.Default.Male
                        "Women" -> Icons.Default.Female
                        "Kids" -> Icons.Default.ChildCare
                        else -> Icons.Default.Watch
                    }
                    Icon(imageVector = iconVector, contentDescription = "", tint = accentColor, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun FlashSaleRowSlider(
    products: List<ProductEntity>,
    wishlist: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    onWishlistToggle: (ProductEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrimsonAlert.copy(alpha = 0.03f))
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FLASHSALE HOT PICKS 🔥", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CrimsonAlert)
            LiveCountdownTimer()
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products) { item ->
                FashionProductCard(
                    product = item,
                    isWishlisted = wishlist.any { it.id == item.id },
                    onProductClick = { onProductClick(item) },
                    onWishlistClick = { onWishlistToggle(item) },
                    modifier = Modifier.width(170.dp)
                )
            }
        }
    }
}

@Composable
fun HorizontalDividerContainer(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = GoldPrimary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 28.dp, bottom = 12.dp)
    )
}

@Composable
fun HorizontalProductsScrollRow(
    products: List<ProductEntity>,
    wishlist: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    onWishlistToggle: (ProductEntity) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(products) { item ->
            FashionProductCard(
                product = item,
                isWishlisted = wishlist.any { it.id == item.id },
                onProductClick = { onProductClick(item) },
                onWishlistClick = { onWishlistToggle(item) },
                modifier = Modifier.width(170.dp)
            )
        }
    }
}

@Composable
fun CartLineItemRow(
    product: ProductEntity,
    item: CartItemEntity,
    variant: ProductVariantEntity?,
    onQtyChange: (Int) -> Unit,
    onDelete: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Virtual item color box matching theme
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getFashionGradient(product.category + product.name, product.id)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.name.take(1), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.brand.uppercase(), fontSize = 9.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                if (variant != null) {
                    Text("Size: ${variant.size}  |  Color: ${variant.color}", fontSize = 11.sp, color = MutedGrey)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // Qty selectors
                    IconButton(onClick = { onQtyChange(item.quantity - 1) }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onQtyChange(item.quantity + 1) }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${(product.discountPrice * item.quantity).toInt()}", fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Save for later icon
                    IconButton(onClick = onToggleSave, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = if (item.savedForLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    // Delete trash button
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BillingSummaryCard(summary: PooshakViewModel.CartSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Billing Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            BillingItemRow(label = "Total Cloth MRP", amount = summary.totalMRP)
            BillingItemRow(label = "- Product Discount Deduct", amount = summary.productDiscount, isDiscount = true)
            
            if (summary.couponDiscount > 0) {
                BillingItemRow(label = "- Promotional Coupon Deduct", amount = summary.couponDiscount, isDiscount = true)
            }

            BillingItemRow(label = "Shipping/Postage Handling", amount = summary.shippingCharge, isFree = summary.shippingCharge == 0.0)

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grand Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("₹${summary.totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldPrimary)
            }
        }
    }
}

@Composable
fun BillingItemRow(label: String, amount: Double, isDiscount: Boolean = false, isFree: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = MutedGrey)
        val amtText = if (isFree) {
            "FREE"
        } else {
            val sgn = if (isDiscount) "- " else ""
            "${sgn}₹${amount.toInt()}"
        }
        Text(
            text = amtText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDiscount || isFree) GreenSuccess else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BillingTotalSection(totalAmount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bag Total (CoD Pay)", fontWeight = FontWeight.Bold)
            Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = GoldPrimary)
        }
    }
}

@Composable
fun ProfileMenuOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, badgeCount: Int = 0, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MutedGrey)
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CrimsonAlert),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "", tint = MutedGrey, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun AddressItemRow(address: AddressEntity, onDelete: () -> Unit, onSetDefault: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(address.fullName, fontWeight = FontWeight.Bold)
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = GoldPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("DEFAULT", color = GoldPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${address.streetAddress}, ${address.city}, ${address.state} - ${address.zipCode}", fontSize = 12.sp, color = MutedGrey)
            Text("Call: ${address.phoneNumber}", fontSize = 11.sp, color = MutedGrey)

            if (!address.isDefault) {
                TextButton(onClick = onSetDefault, contentPadding = PaddingValues(0.dp)) {
                    Text("Make Default Address", fontSize = 11.sp, color = GoldPrimary)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryRow(order: OrderEntity, onTrackClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onTrackClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Receipt, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Order: #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Cash Total: ₹${order.totalAmount.toInt()}", fontSize = 11.sp, color = MutedGrey)
            }
            Surface(
                color = if (order.status == "Delivered") GreenSuccess.copy(alpha = 0.12f) else if (order.status == "Cancelled") CrimsonAlert.copy(alpha = 0.12f) else GoldSecondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                val clr = if (order.status == "Delivered") GreenSuccess else if (order.status == "Cancelled") CrimsonAlert else GoldPrimary
                Text(order.status.uppercase(), color = clr, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun TimelineNodeItem(title: String, subtitle: String, isCompleted: Boolean, isLast: Boolean) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) GreenSuccess else MutedGrey.copy(alpha = 0.3f))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (isCompleted) GreenSuccess else MutedGrey.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MutedGrey, fontSize = 13.sp)
            Text(subtitle, color = MutedGrey, fontSize = 11.sp)
        }
    }
}

@Composable
fun SuggestionChipItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightGreyAccent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CharcoalDark)
    }
}

@Composable
fun DialogAddAddress(onDismiss: () -> Unit, onSave: (String, String, String, String, String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateStr by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var makeDefault by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Full Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, placeholder = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = street, onValueChange = { street = it }, placeholder = { Text("Street, Apartment") })
                OutlinedTextField(value = city, onValueChange = { city = it }, placeholder = { Text("City") })
                OutlinedTextField(value = stateStr, onValueChange = { stateStr = it }, placeholder = { Text("State") })
                OutlinedTextField(value = zip, onValueChange = { zip = it }, placeholder = { Text("Pincode / Zip") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = makeDefault, onCheckedChange = { makeDefault = it })
                    Text("Make Default Address", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, street, city, stateStr, zip, makeDefault) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("SAVE", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DialogAdvancedFilters(viewModel: PooshakViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Clothing Closet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Price Range
                Text("Max Price: ₹${viewModel.filterPriceMax.value.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Slider(
                    value = viewModel.filterPriceMax.value,
                    onValueChange = { viewModel.filterPriceMax.value = it },
                    valueRange = 500f..10000f,
                    colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                )

                // Gender Switch Row
                Text("Gender Collection", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Men", "Women", "Kids", "Accessories").forEach { gen ->
                        val matched = viewModel.filterGender.value == gen
                        FilterChip(
                            selected = matched,
                            onClick = { viewModel.filterGender.value = if (matched) "" else gen },
                            label = { Text(gen, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary)
                        )
                    }
                }

                // Premium Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = viewModel.filterPremiumOnly.value, onCheckedChange = { viewModel.filterPremiumOnly.value = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                    Text("Premium Collection Only", fontSize = 12.sp)
                }

                // Discount Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = viewModel.filterDiscountOnly.value, onCheckedChange = { viewModel.filterDiscountOnly.value = it }, colors = CheckboxDefaults.colors(checkedColor = GoldPrimary))
                    Text("Discounted Sale Cuts Only", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                Text("APPLY FILTERS", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.clearSearchAndFilters(); onDismiss() }) {
                Text("Clear All")
            }
        }
    )
}

fun getTimelineStageMessage(stage: String): String = when(stage) {
    "Confirmed" -> "Apparel item verified. Preparing to pack."
    "Packed" -> "Double lined heavy couture safely boxed."
    "Shipped" -> "In transit via Pooshak Express."
    "Delivered" -> "Delivered. Thank you for shopping with us!"
    else -> "Awaiting update."
}
