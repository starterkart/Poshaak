package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: PooshakViewModel, productId: Long) {
    val p = viewModel.activeProduct.value
    val context = LocalContext.current
    var showSizeChart by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Check if item is wishlisted reactively
    val isWishlisted = viewModel.wishlistItems.any { it.id == p?.id }

    if (p == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldPrimary)
        }
    } else {
        // Find selected variant's individual inventory stock tracker
        val activeVariant = viewModel.activeVariants.find {
            it.size == viewModel.selectedSize.value && it.color == viewModel.selectedColor.value
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(p.name.take(20) + "...", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.toggleProductWishlist(p.id) { _ -> }
                        }) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (isWishlisted) CrimsonAlert else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Share Intent Hook
                        IconButton(onClick = {
                            val msg = "Check out this beautiful ${p.name} from Pooshak store for ₹${p.discountPrice.toInt()}!"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Threads via..."))
                        }) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    // 1. MULTIPLE SWIPING IMAGE MATRICES
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(getFashionGradient(p.category, p.id))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p.name.take(1),
                                fontSize = 110.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.1f)
                            )
                            Text(
                                text = p.brand.uppercase(),
                                fontSize = 12.sp,
                                letterSpacing = 4.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        // Swiping indicators
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(3) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == 0) GoldSecondary else Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }

                    // 2. METADATA BLOCK
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = p.brand.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = p.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Price Line
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${p.discountPrice.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (p.price > p.discountPrice) {
                                Text(
                                    text = "₹${p.price.toInt()}",
                                    fontSize = 15.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MutedGrey
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                val pct = (((p.price - p.discountPrice) / p.price) * 100).toInt()
                                Text(
                                    text = "$pct% OFF",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAlert
                                )
                            }
                        }

                        // 3. INTEGRATED STOCK & DELIVERY ESTIMATE FLAGS
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SoftChalk)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Outlined.LocalShipping, contentDescription = "", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Free delivery estimate", fontSize = 12.sp)
                                }
                                Text("Within ${p.deliveryDays} Days", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenSuccess)
                            }
                        }

                        // 4. SIZE & COLOR SELECTION (PRODUCT VARIANT MATRIX SYSTEM)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Size", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { showSizeChart = true }) {
                                Text("SIZE CHART", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        val availableSizes = remember(viewModel.activeVariants) {
                            viewModel.activeVariants.map { it.size }.distinct()
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            availableSizes.forEach { sz ->
                                val isSelected = viewModel.selectedSize.value == sz
                                val isEnabled = viewModel.activeVariants.any { it.size == sz && it.stock > 0 }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else if (isEnabled) LightGreyAccent else Color.Transparent)
                                        .border(2.dp, if (isSelected) GoldPrimary else if (isEnabled) SoftChalk else SoftChalk.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .clickable(enabled = isEnabled) { viewModel.selectedSize.value = sz }
                                        .testTag("size_chip_$sz"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sz,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else if (isEnabled) CharcoalDark else MutedGrey.copy(alpha = 0.3f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Colors select row
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Select Color Variant", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        val availableColors = remember(viewModel.activeVariants) {
                            viewModel.activeVariants.map { it.color }.distinct()
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            availableColors.forEach { col ->
                                val isSelected = viewModel.selectedColor.value == col
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectedColor.value = col },
                                    label = { Text(col) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // 5. INVENTORY REMINDER LABEL
                        Spacer(modifier = Modifier.height(16.dp))
                        if (activeVariant != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (activeVariant.stock > 4) GreenSuccess else CrimsonAlert)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val stockMsg = when {
                                    activeVariant.stock <= 0 -> "OUT OF STOCK"
                                    activeVariant.stock <= 4 -> "HURRY: ONLY ${activeVariant.stock} DECOURES LEFT!"
                                    else -> "In Stock (${activeVariant.stock} options tracking)"
                                }
                                Text(
                                    text = stockMsg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeVariant.stock > 4) MaterialTheme.colorScheme.onSurface else CrimsonAlert
                                )
                            }
                        }

                        // 6. DETAILED COUTURE DESCRIPTION
                        Spacer(modifier = Modifier.height(28.dp))
                        Text("Sartorial Specifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = p.description,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fabric Weight / Assembly: ", fontSize = 12.sp, color = MutedGrey)
                            Text(p.fabric, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 7. VERIFIED REVIEWS SYSTEM & SCORE RATINGS
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Verified Buyer Reviews (${p.reviewsCount})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { showReviewDialog = true }) {
                                Text("WRITE REVIEW", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (viewModel.activeReviews.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    "No reviews recorded yet for this piece. Buy this item and leave a verified purchase report!",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 12.sp,
                                    color = MutedGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            viewModel.activeReviews.forEach { rev ->
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
                                            Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Row {
                                                repeat(rev.rating) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = "", tint = GoldSecondary, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                        if (rev.isVerifiedPurchase) {
                                            Surface(
                                                color = GreenSuccess.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Text("✓ VERIFIED PURCHASE BY CLIENT", color = GreenSuccess, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(rev.comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. PERSISTENT PURCHASE BAR
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Add to cart Button (working)
                        Button(
                            onClick = {
                                viewModel.addToCartSelectedVariant(
                                    onCompleted = {
                                        viewModel.navigateTo(Screen.Main)
                                        viewModel.currentTab.value = MainTab.CART
                                    },
                                    onError = {
                                        // Handle out of stock alerts
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("add_to_cart_detail_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.ShoppingBag, contentDescription = "", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD TO CART", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Buy Now fast checkout trigger
                        Button(
                            onClick = {
                                viewModel.addToCartSelectedVariant(
                                    onCompleted = {
                                        viewModel.navigateTo(Screen.Checkout)
                                    },
                                    onError = {
                                        // Out of stock
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("buy_now_detail_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("BUY NOW", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal size helper
    if (showSizeChart) {
        DialogSizeChart(onDismiss = { showSizeChart = false })
    }

    // Reviews writer
    if (showReviewDialog) {
        DialogAddReview(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitVerifiedReview(p!!.id, rating, comment) { res ->
                    showReviewDialog = false
                }
            }
        )
    }
}

@Composable
fun DialogSizeChart(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apparel Size Chart (cm)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth().background(LightGreyAccent).padding(6.dp)) {
                    Text("SIZE", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("CHEST", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("LENGTH", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    Text("S", modifier = Modifier.weight(1f))
                    Text("92 - 96", modifier = Modifier.weight(1.2f))
                    Text("68", modifier = Modifier.weight(1.2f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    Text("M", modifier = Modifier.weight(1f))
                    Text("98 - 102", modifier = Modifier.weight(1.2f))
                    Text("70", modifier = Modifier.weight(1.2f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    Text("L", modifier = Modifier.weight(1f))
                    Text("104 - 108", modifier = Modifier.weight(1.2f))
                    Text("72", modifier = Modifier.weight(1.2f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                    Text("XL", modifier = Modifier.weight(1f))
                    Text("110 - 114", modifier = Modifier.weight(1.2f))
                    Text("74", modifier = Modifier.weight(1.2f))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                Text("GOT IT", color = Color.White)
            }
        }
    )
}

@Composable
fun DialogAddReview(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write Verified Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Rating Score:")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { score ->
                        IconButton(onClick = { rating = score }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "",
                                tint = if (score <= rating) GoldSecondary else MutedGrey.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Share honest feedback details with other clients...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Text(
                    "Note: Reviews are only permitted for accounts with a successfully delivered order of this fashion piece.",
                    fontSize = 10.sp,
                    color = CrimsonAlert
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)) {
                Text("SUBMIT", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
