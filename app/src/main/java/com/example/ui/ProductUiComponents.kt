package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FashionProductCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onWishlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Visual Image placeholder using colored linear gradients reflecting the fashion theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(getFashionGradient(product.category, product.id))
            ) {
                // Diagonal Overlay lines for luxury visual textured background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 1.dp.toPx()
                    // Warm texture lines
                    for (i in 0..size.width.toInt() step 50) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(i.toFloat(), 0f),
                            end = androidx.compose.ui.geometry.Offset(i.toFloat() + 100f, size.height),
                            strokeWidth = stroke
                        )
                    }
                }

                // Styled floating hanger icon in positive space for real luxury catalog feel
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = product.name.take(1),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Wishlist Hover Overlay Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable(onClick = onWishlistClick)
                        .testTag("wish_button_id_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save Product",
                        tint = if (isWishlisted) CrimsonAlert else CharcoalDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Floating Luxury Status Tags
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isPremium) {
                        Surface(
                            color = GoldPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LUXURY",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (product.isBestseller) {
                        Surface(
                            color = GreenSuccess,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "BESTSELLER",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Text Metadata Sections
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.brand.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                // Price Section side-by-side
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.discountPrice.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (product.price > product.discountPrice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = MutedGrey
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        val pct = (((product.price - product.discountPrice) / product.price) * 100).toInt()
                        Text(
                            text = "($pct% OFF)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonAlert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mini Star rating line
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = GoldSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "%.1f".format(product.rating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewsCount})",
                        fontSize = 11.sp,
                        color = MutedGrey
                    )
                }
            }
        }
    }
}

@Composable
fun LiveCountdownTimer() {
    var timeLeft by remember { mutableStateOf(8315) } // Approx 2hr 18min 35s

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    val hr = timeLeft / 3600
    val min = (timeLeft % 3600) / 60
    val sec = timeLeft % 60

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CrimsonAlert.copy(alpha = 0.1f))
            .border(1.dp, CrimsonAlert.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(CrimsonAlert)
        )
        Text(
            text = "FLASH SALE: ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CrimsonAlert,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "%02dh : %02dm : %02ds".format(hr, min, sec),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CrimsonAlert,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

// Generate premium clothing color gradients for realistic product cards
fun getFashionGradient(category: String, id: Long): Brush {
    val seedIndex = (id % 5).toInt()
    val starts = listOf(
        Color(0xFF384358), // Slate Blue
        Color(0xFF4C3B4E), // Wine Plum
        Color(0xFF354E41), // Olive Moss
        Color(0xFF55443F), // Warm Roast Coffee
        Color(0xFF2C2D35)  // Graphite Charcoal
    )
    val ends = listOf(
        Color(0xFF222834),
        Color(0xFF2E2330),
        Color(0xFF1E2D25),
        Color(0xFF362B28),
        Color(0xFF16171B)
    )

    return Brush.verticalGradient(
        colors = listOf(starts[seedIndex], ends[seedIndex])
    )
}
