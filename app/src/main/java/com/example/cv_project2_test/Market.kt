// Market.kt - ProductManager와 연동된 버전
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import android.net.Uri
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
import androidx.compose.ui.unit.times
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

// 데이터 클래스들 - imageUri 필드 추가
data class MarketProduct(
    val name: String,
    val seller: String,
    val distance: String,
    val imageUrl: String,
    val drawableRes: Int? = null,
    val imageUri: Uri? = null  // 실제 촬영된 이미지 추가
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
    onNavigateToProductRegister: () -> Unit = {},
    onProductClick: (MarketProduct) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onBottomNavClick: (String) -> Unit = {}
) {
    // ProductManager에서 등록된 상품들 가져오기
    val registeredProducts = ProductManager.products

    // 등록된 상품을 MarketProduct로 변환
    val marketProducts = registeredProducts.map { productReg ->
        MarketProduct(
            name = if (productReg.isOrganic) "${productReg.cropType} (유기농)" else productReg.cropType,
            seller = "내 농장",
            distance = "0.1km",
            imageUrl = "https://placehold.co/240x320",
            drawableRes = getDrawableForCrop(productReg.cropType),
            imageUri = productReg.images.firstOrNull() // 첫 번째 이미지 사용
        )
    }

    // 기본 샘플 상품들 (등록된 상품이 없을 때만 표시)
    val defaultProducts = if (marketProducts.isEmpty()) {
        listOf(
            MarketProduct("방울토마토 샘플", "샘플 농장", "0.5km", "https://placehold.co/240x320", R.drawable.tomato),
            MarketProduct("바질 샘플", "샘플 허브농장", "1.2km", "https://placehold.co/240x320", R.drawable.basil1)
        )
    } else emptyList()

    // 추천 상품 (등록된 상품이 있으면 최신 3개, 없으면 기본 상품)
    val featuredProducts = if (marketProducts.isNotEmpty()) {
        marketProducts.take(3)
    } else defaultProducts

    // 인기 상품 (나머지 등록된 상품들)
    val popularProducts = if (marketProducts.isNotEmpty()) {
        marketProducts.drop(3)
    } else emptyList()

    // 카테고리 칩들 - 한국어로 변경
    val categories = listOf(
        CategoryChip("토마토", "🍅"),
        CategoryChip("상추", "🥬"),
        CategoryChip("오이", "🥒"),
        CategoryChip("당근", "🥕"),
        CategoryChip("감자", "🥔"),
        CategoryChip("양파", "🧅"),
        CategoryChip("배추", "🥗"),
        CategoryChip("기타", "🌿")
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
            if (featuredProducts.isNotEmpty()) {
                item {
                    MarketSectionTitle(
                        if (marketProducts.isNotEmpty()) "최근 등록된 농산물" else "추천 농산물"
                    )
                }
                item {
                    FeaturedProductsSection(
                        products = featuredProducts,
                        onProductClick = onProductClick
                    )
                }
            }

            // 카테고리 옵션
            item {
                CategorySection(
                    categories = categories,
                    onCategoryClick = onCategoryClick
                )
            }

            // 인기 상품 그리드 또는 빈 상태 메시지
            if (popularProducts.isNotEmpty()) {
                item {
                    MarketSectionTitle("다른 농산물")
                }
                item {
                    PopularProductsGrid(
                        products = popularProducts,
                        onProductClick = onProductClick
                    )
                }
            } else if (marketProducts.isEmpty()) {
                item {
                    EmptyStateMessage(onNavigateToProductRegister)
                }
            }
        }
    }
}

// 작물 이름에 따른 drawable 리소스 반환
fun getDrawableForCrop(cropName: String): Int? {
    return when (cropName) {
        "토마토" -> R.drawable.tomato
        "바질" -> R.drawable.basil1
        else -> null
    }
}

@Composable
fun EmptyStateMessage(onNavigateToProductRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MarketColors.Secondary
        )

        Text(
            text = "직접 등록한 농산물을 마켓에서 확인해보세요!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MarketColors.OnSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "첫 번째 농산물을 등록해보세요",
            fontSize = 14.sp,
            color = MarketColors.Secondary,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onNavigateToProductRegister,
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF738903)
            )
        ) {
            Text("농산물 등록하기")
        }
    }
}

@Composable
fun HeaderSection(
    onBackClick: () -> Unit,
    onProductRegisterClick: () -> Unit
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
            onClick = onProductRegisterClick,
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
                    text = "찾으시는 농산물이 있나요?",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상품 이미지 - 실제 촬영 이미지 우선, 그 다음 drawable, 마지막에 URL
            AsyncImage(
                model = product.imageUri ?: product.drawableRes ?: product.imageUrl,
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
    val gridHeight = ((products.size + 1) / 2) * 300.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 상품 이미지
            AsyncImage(
                model = product.imageUri ?: product.drawableRes ?: product.imageUrl,
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
        MarketNavItem("Market", Icons.Default.ShoppingCart, true),
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
fun WeCanFarmMarketScreenPreview() {
    WeCanFarmMarketScreen()
}