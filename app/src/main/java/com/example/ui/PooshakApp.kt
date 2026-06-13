package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PooshakApp(viewModel: PooshakViewModel) {
    val activeScreen = viewModel.backStack.lastOrNull() ?: Screen.Splash
    val currentSelectedTab = viewModel.currentTab.value

    // Interactive System Hardware back key intercepting
    BackHandler(enabled = viewModel.backStack.size > 1) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Full edge-to-edge
        bottomBar = {
            // Render the M3 Navigation bar ONLY inside Screen.Main
            if (activeScreen == Screen.Main) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding() // Respect device navigation pill insets
                        .testTag("pooshak_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // TAB: HOME
                    NavigationBarItem(
                        selected = currentSelectedTab == MainTab.HOME,
                        onClick = { viewModel.currentTab.value = MainTab.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentSelectedTab == MainTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // TAB: CATEGORIES
                    NavigationBarItem(
                        selected = currentSelectedTab == MainTab.CATEGORIES,
                        onClick = { viewModel.currentTab.value = MainTab.CATEGORIES },
                        icon = {
                            Icon(
                                imageVector = if (currentSelectedTab == MainTab.CATEGORIES) Icons.Default.GridView else Icons.Outlined.GridView,
                                contentDescription = "Categories"
                            )
                        },
                        label = { Text("Categories", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_tab_categories")
                    )

                    // TAB: WISHLIST
                    NavigationBarItem(
                        selected = currentSelectedTab == MainTab.WISHLIST,
                        onClick = { viewModel.currentTab.value = MainTab.WISHLIST },
                        icon = {
                            Icon(
                                imageVector = if (currentSelectedTab == MainTab.WISHLIST) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist"
                            )
                        },
                        label = { Text("Wishlist", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_tab_wishlist")
                    )

                    // TAB: CART
                    NavigationBarItem(
                        selected = currentSelectedTab == MainTab.CART,
                        onClick = { viewModel.currentTab.value = MainTab.CART },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (viewModel.cartItems.isNotEmpty()) {
                                        Badge(containerColor = GoldPrimary) {
                                            Text(viewModel.cartItems.filter { !it.first.savedForLater }.size.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentSelectedTab == MainTab.CART) Icons.Default.ShoppingBag else Icons.Outlined.ShoppingBag,
                                    contentDescription = "Cart"
                                )
                            }
                        },
                        label = { Text("Bag", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_tab_cart")
                    )

                    // TAB: PROFILE
                    NavigationBarItem(
                        selected = currentSelectedTab == MainTab.PROFILE,
                        onClick = { viewModel.currentTab.value = MainTab.PROFILE },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (viewModel.unreadNotificationsCount.value > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text(viewModel.unreadNotificationsCount.value.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentSelectedTab == MainTab.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        },
                        label = { Text("Profile", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldPrimary,
                            selectedTextColor = GoldPrimary,
                            indicatorColor = GoldPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        // Premium Animated Navigation state switching
        AnimatedContent(
            targetState = activeScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) { targetScreen ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (targetScreen) {
                    is Screen.Splash -> SplashScreen(viewModel = viewModel)
                    is Screen.Login -> LoginScreen(viewModel = viewModel)
                    is Screen.Signup -> SignupScreen(viewModel = viewModel)
                    is Screen.ForgotPassword -> ForgotPasswordScreen(viewModel = viewModel)
                    is Screen.Main -> {
                        // Inner tab switcher inside Screen.Main holding Scaffold offsets
                        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                            when (currentSelectedTab) {
                                MainTab.HOME -> TabHomeScreen(viewModel = viewModel)
                                MainTab.CATEGORIES -> TabCategoriesScreen(viewModel = viewModel)
                                MainTab.WISHLIST -> TabWishlistScreen(viewModel = viewModel)
                                MainTab.CART -> TabCartScreen(viewModel = viewModel)
                                MainTab.PROFILE -> TabProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                    is Screen.ProductDetails -> ProductDetailScreen(viewModel = viewModel, productId = targetScreen.productId)
                    is Screen.AdvancedSearch -> AdvancedSearchScreen(viewModel = viewModel)
                    is Screen.Checkout -> CheckoutScreen(viewModel = viewModel)
                    is Screen.OrderTracking -> OrderTrackingScreen(viewModel = viewModel)
                    is Screen.Notifications -> NotificationsScreen(viewModel = viewModel)
                    is Screen.HelpCenter -> HelpCenterScreen(viewModel = viewModel)
                }
            }
        }
    }
}
