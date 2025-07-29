// Market.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// 마켓 화면용 색상
object MarketColors {
    val Surface = Color.White
    val OnSurface = Color(0xFF111614)
    val Secondary = Color(0xFF608970)
    val SearchBackground = Color(0xFFEFF4F2)
    val CategoryBackground = Color(0xFFEFF4F2)
    val BottomNavBorder = Color(0xFFEFF4F2)
}

// 데이터 클래스들
data class MarketProduct(
    val name: String,
    val seller: String,
    val distance: String,
    val imageUrl: String
)

data class CategoryChip(
    val name: String,
    val emoji: String
)

data class MarketNavItem(
    val label: String,
    val icon: ImageVector,
    val isSelected: Boolean
)

@Composable
fun WeCanFarmMarketScreen(
    onBackClick: () -> Unit = {},
    onNavigateToProductRegister: () -> Unit = {},  // 상품등록 화면으로 이동하는 콜백
    onProductClick: (MarketProduct) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onBottomNavClick: (String) -> Unit = {}
) {
    // 상단 배너 상품들
    val featuredProducts = listOf(
        MarketProduct("Homegrown Cherry Tomatoes", "John's Garden", "0.5km", "https://placehold.co/240x320"),
        MarketProduct("Fresh Basil", "Sarah's Herbs", "1.2km", "https://placehold.co/240x320"),
        MarketProduct("Ripe Strawberries", "David's Farm", "0.8km", "https://placehold.co/240x320")
    )

    // 카테고리 칩들
    val categories = listOf(
        CategoryChip("Vegetables", "🍅"),
        CategoryChip("Fruits", "🍎"),
        CategoryChip("Herbs", "🌿"),
        CategoryChip("Seedlings", "🌱")
    )

    // 인기 상품들 (그리드용)
    val popularProducts = listOf(
        MarketProduct("Organic Carrots", "Emily's Farm", "0.7km", "https://placehold.co/173x231"),
        MarketProduct("Heirloom Tomatoes", "Mark's Garden", "1.1km", "https://placehold.co/173x231"),
        MarketProduct("Fresh Basil", "Sophia's Herbs", "0.9km", "https://placehold.co/173x231"),
        MarketProduct("Local Honey", "Ethan's Apiary", "1.5km", "https://placehold.co/173x231"),
        MarketProduct("Free-Range Eggs", "Olivia's Coop", "0.6km", "https://placehold.co/173x231"),
        MarketProduct("Microgreens", "Noah's Greens", "1.3km", "https://placehold.co/173x231")
    )

    Scaffold(
        bottomBar = { BottomNavigationBar(onBottomNavClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MarketColors.Surface)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 헤더
            item {
                HeaderSection(
                    onBackClick = onBackClick,
                    onProductRegisterClick = onNavigateToProductRegister
                )
            }

            // 검색바
            item {
                SearchSection(onSearchClick = { /* TODO: 검색 기능 */ })
            }

            // 추천 상품 (가로 스크롤)
            item {
                FeaturedProductsSection(
                    products = featuredProducts,
                    onProductClick = onProductClick
                )
            }

            // 카테고리 옵션
            item {
                CategorySection(
                    categories = categories,
                    onCategoryClick = onCategoryClick
                )
            }

            // "Popular near you" 제목
            item {
                MarketSectionTitle("Popular near you")
            }

            // 인기 상품 그리드
            item {
                PopularProductsGrid(
                    products = popularProducts,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    onBackClick: () -> Unit,
    onProductRegisterClick: () -> Unit  // 상품등록 버튼 클릭 콜백
) {
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
                tint = MarketColors.OnSurface
            )
        }

        // 제목
        Text(
            text = "Fresh Market",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MarketColors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // 상품 등록 버튼
        Button(
            onClick = onProductRegisterClick,  // ProductRegister 화면으로 이동
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF738903),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                text = "상품 등록",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SearchSection(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MarketColors.SearchBackground,
            onClick = onSearchClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MarketColors.Secondary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "What are you looking for?",
                    fontSize = 16.sp,
                    color = MarketColors.Secondary
                )
            }
        }
    }
}

@Composable
fun FeaturedProductsSection(
    products: List<MarketProduct>,
    onProductClick: (MarketProduct) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products.size) { index ->
            FeaturedProductCard(
                product = products[index],
                onClick = { onProductClick(products[index]) }
            )
        }
    }
}

@Composable
fun FeaturedProductCard(
    product: MarketProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(400.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MarketColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상품 이미지
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // 상품 정보
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MarketColors.OnSurface
                )

                Text(
                    text = "${product.seller} • ${product.distance}",
                    fontSize = 14.sp,
                    color = MarketColors.Secondary
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    categories: List<CategoryChip>,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.size) { index ->
            CategoryChipItem(
                category = categories[index],
                onClick = { onCategoryClick(categories[index].name) }
            )
        }
    }
}

@Composable
fun CategoryChipItem(
    category: CategoryChip,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(12.dp),
        color = MarketColors.CategoryBackground,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${category.emoji} ${category.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MarketColors.OnSurface
            )
        }
    }
}

@Composable
fun MarketSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MarketColors.OnSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun PopularProductsGrid(
    products: List<MarketProduct>,
    onProductClick: (MarketProduct) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(800.dp) // 고정 높이로 LazyColumn 내에서 사용
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products.size) { index ->
            PopularProductCard(
                product = products[index],
                onClick = { onProductClick(products[index]) }
            )
        }
    }
}

@Composable
fun PopularProductCard(
    product: MarketProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MarketColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 상품 이미지
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(231.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // 상품 정보
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MarketColors.OnSurface
                )

                Text(
                    text = "${product.seller} • ${product.distance}",
                    fontSize = 14.sp,
                    color = MarketColors.Secondary
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onBottomNavClick: (String) -> Unit) {
    val navItems = listOf(
        MarketNavItem("Home", Icons.Default.Home, false),
        MarketNavItem("Diagnose", Icons.Default.Search, false),
        MarketNavItem("Market", Icons.Default.ShoppingCart, true), // 현재 선택됨
        MarketNavItem("Community", Icons.Default.AccountCircle, false),
        MarketNavItem("Profile", Icons.Default.Person, false)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MarketColors.Surface,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MarketColors.BottomNavBorder)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    MarketBottomNavItem(
                        item = item,
                        onClick = { onBottomNavClick(item.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MarketBottomNavItem(
    item: MarketNavItem,
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
                tint = if (item.isSelected) MarketColors.OnSurface else MarketColors.Secondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (item.isSelected) MarketColors.OnSurface else MarketColors.Secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmFScreenPreview() {
    WeCanFarmMarketScreen()
}