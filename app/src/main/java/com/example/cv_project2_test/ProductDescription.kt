// ProductDescription.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// 상품 상세 화면용 색상
object ProductColors {
    val Primary = Color(0xFFD9D277)
    val Surface = Color.White
    val OnSurface = Color(0xFF111614)
    val Secondary = Color(0xFF608970)
    val ChipBackground = Color(0xFFEFF4F2)
    val BottomNavBorder = Color(0xFFEFF4F2)
    val GradientOverlay = Color.Black.copy(alpha = 0.4f)
}

// 데이터 클래스들
data class ProductDetail(
    val title: String,
    val seller: String,
    //val distance: String,
    val rating: Float,
    val description: String,
    val story: String,
    val images: List<Any>,
    val avatarUrl: String = ""
)

data class PickupOption(
    val name: String,
    val isSelected: Boolean = false
)

data class ReviewItem(
    val userName: String,
    val timeAgo: String,
    val rating: Float,
    val comment: String,
    val likes: Int,
    val avatarUrl: String = "https://placehold.co/40x40"
)

data class ProductNavItem(
    val label: String,
    val icon: ImageVector,
    val isSelected: Boolean
)

@Composable
fun WeCanFarmProductDescriptionScreen(
    onBackClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onPickupOptionClick: (String) -> Unit = {},
    onBottomNavClick: (String) -> Unit = {}
) {
    val product = ProductDetail(
        title = "Cherry Tomatoes",
        seller = "Meet Sarah 👩‍🌾",

        rating = 4.9f,
        description = "Picked yesterday morning | No pesticides | 2kg available",
        story = "I've grow this cherry tomato during 4 years",
        images = listOf(
            R.drawable.tomato,
            R.drawable.tomato2,
        )
    )

    val pickupOptions = listOf(
        PickupOption("Today", true),
        PickupOption("Tomorrow"),
        PickupOption("Next week")
    )

    val reviews = listOf(
        ReviewItem(
            userName = "Emily Carter",
            timeAgo = "2 weeks ago",
            rating = 5f,
            comment = "Amazing tomatoes, so fresh! They were the perfect addition to my summer salad. I'll definitely be ordering again.",
            likes = 2
        ),
//        ReviewItem(
//            userName = "David Lee",
//            timeAgo = "1 month ago",
//            rating = 5f,
//            comment = "These cherry tomatoes are the best I've ever had. The flavor is incredible, and you can tell they were grown with care. Highly recommend!",
//            likes = 1
//        )
    )

    Scaffold(
//        bottomBar = { ProductBottomNavigationBar(onBottomNavClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ProductColors.Surface)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 헤더
            item {
                ProductHeaderSection(onBackClick = onBackClick)
            }

            // 이미지 갤러리
            item {
                ProductImageGallery(images = product.images)
            }

            // 상품 제목
            item {
                ProductTitleSection(title = product.title)
            }

            // 판매자 정보
            item {
                SellerInfoSection(
                    seller = product.seller,
                    rating = product.rating,
                    avatarUrl = ""
                )
            }

            // 상품 설명
            item {
                ProductDescriptionSection(description = product.description)
            }

            // 재배 이야기(Grow Story)
            item {
                GrowingStorySection(story = product.story)
            }

            // 메시지 버튼
            item {
                MessageButtonSection(onMessageClick = onMessageClick)
            }

            // 리뷰 섹션
            item {
                ReviewsSection(reviews = reviews)
            }
        }
    }
}

@Composable
fun ProductHeaderSection(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ProductColors.OnSurface
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 더보기 메뉴 버튼
        IconButton(
            onClick = { /* TODO: More menu */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More",
                tint = ProductColors.OnSurface
            )
        }
    }
}

@Composable
fun ProductImageGallery(images: List<Any>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // 메인 이미지
        AsyncImage(
            model = images.firstOrNull(),
            contentDescription = "Product Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 그라데이션 오버레이 (하단)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ProductColors.GradientOverlay
                        ),
                        startY = 200f
                    )
                )
        )

        // 페이지 인디케이터
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (index == 0) Color.White else Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun ProductTitleSection(title: String) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = ProductColors.OnSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    )
}

@Composable
fun SellerInfoSection(
    seller: String,
    rating: Float,
    avatarUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아바타
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Seller Avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // 판매자 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = seller,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ProductColors.OnSurface
            )
        }
    }
}

@Composable
fun ProductDescriptionSection(description: String) {
    Text(
        text = description,
        fontSize = 16.sp,
        color = ProductColors.OnSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
fun GrowingStorySection(story: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Growing story",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ProductColors.OnSurface,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Text(
            text = story,
            fontSize = 16.sp,
            color = ProductColors.OnSurface,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun MessageButtonSection(onMessageClick: () -> Unit) {
    Button(
        onClick = onMessageClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ProductColors.Primary,
            contentColor = ProductColors.OnSurface
        )
    ) {
        Text(
            text = "💬 Message to Farmer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PickupOptionsSection(
    options: List<PickupOption>,
    onOptionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "When can you pick up?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ProductColors.OnSurface,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        LazyRow(
            modifier = Modifier.padding(start = -4.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(options.size) { index ->
                PickupOptionChip(
                    option = options[index],
                    onClick = { onOptionClick(options[index].name) }
                )
            }
        }
    }
}

@Composable
fun PickupOptionChip(
    option: PickupOption,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(12.dp),
        color = ProductColors.ChipBackground,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ProductColors.OnSurface
            )
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<ReviewItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Reviews",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ProductColors.OnSurface,
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            reviews.forEach { review ->
                ReviewCard(review = review)
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 사용자 정보
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = review.avatarUrl,
                contentDescription = "${review.userName} avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = review.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProductColors.OnSurface
                )

                Text(
                    text = review.timeAgo,
                    fontSize = 14.sp,
                    color = ProductColors.Secondary
                )
            }
        }

        // 별점
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Star",
                    tint = if (index < review.rating) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 리뷰 내용
        Text(
            text = review.comment,
            fontSize = 16.sp,
            color = ProductColors.OnSurface,
            lineHeight = 24.sp
        )

        // 좋아요 및 답글
        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Like",
                    tint = ProductColors.Secondary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = review.likes.toString(),
                    fontSize = 16.sp,
                    color = ProductColors.Secondary
                )
            }

            TextButton(
                onClick = { /* TODO: Reply action */ }
            ) {
                Text(
                    text = "Reply",
                    fontSize = 16.sp,
                    color = ProductColors.Secondary
                )
            }
        }
    }
}

//@Composable
//fun ProductBottomNavigationBar(onBottomNavClick: (String) -> Unit) {
//    val navItems = listOf(
//        ProductNavItem("Home", Icons.Default.Home, false),
//        ProductNavItem("Diagnose", Icons.Default.Search, false),
//        ProductNavItem("Market", Icons.Default.ShoppingCart, true), // 현재 선택됨
//        ProductNavItem("Community", Icons.Default.AccountCircle, false),
//        ProductNavItem("Profile", Icons.Default.Person, false)
//    )
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        color = ProductColors.Surface,
//        shadowElevation = 8.dp
//    ) {
//        Column {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .border(1.dp, ProductColors.BottomNavBorder)
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                navItems.forEach { item ->
//                    ProductBottomNavItem(
//                        item = item,
//                        onClick = { onBottomNavClick(item.label) }
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//        }
//    }
//}

@Composable
fun ProductBottomNavItem(
    item: ProductNavItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (item.isSelected) ProductColors.OnSurface else ProductColors.Secondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (item.isSelected) ProductColors.OnSurface else ProductColors.Secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmProductDescriptionScreenPreview() {
    WeCanFarmProductDescriptionScreen()
}