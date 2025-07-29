// OnBoarding2.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// 온보딩2 화면용 색상
object OnBoarding2Colors {
    val Primary = Color(0xFFD9D277)
    val Surface = Color.White
    val OnSurface = Color(0xFF161611)
    val Secondary = Color(0xFF7F8763)
    val Indicator = Color(0xFFE2E5DB)
}

// 기능 소개 데이터 클래스
data class FeatureItem(
    val title: String,
    val description: String,
    val imageUrl: String = "https://placehold.co/130x66",
    val drawableRes: Int? = null  // drawable 리소스 ID 추가
)

@Composable
fun WeCanFarmOnBoarding2Screen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val features = listOf(
        FeatureItem(
            title = "Plant Doctor",
            description = "AI diagnoses plant problems instantly",
            drawableRes = R.drawable.plantdoctor  // Plant Doctor 이미지
        ),
        FeatureItem(
            title = "Local Market",
            description = "Buy & sell fresh produce with neighbors",
            drawableRes = R.drawable.localmarket  // Local Market 이미지 (추가 필요)
        ),
        FeatureItem(
            title = "Community",
            description = "Get advice from experienced growers",
            drawableRes = R.drawable.community  // Community 이미지 (추가 필요)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OnBoarding2Colors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OnBoarding2Colors.Surface)
        ) {
            // 헤더
            HeaderSection(onBackClick = onBackClick)

            // 메인 컨텐츠
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 기능 리스트
                items(features.size) { index ->
                    FeatureCard(
                        feature = features[index],
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 페이지 인디케이터
                item {
                    PageIndicator2(
                        currentPage = 1,
                        totalPages = 2,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }

            // 하단 버튼
            BottomSection(onNextClick = onNextClick)
        }
    }
}

@Composable
fun HeaderSection(onBackClick: () -> Unit) {
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
                tint = OnBoarding2Colors.OnSurface
            )
        }

        // 제목
        Text(
            text = "What can you do here?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = OnBoarding2Colors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 48.dp) // 뒤로가기 버튼과 균형 맞춤
        )
    }
}

@Composable
fun FeatureCard(
    feature: FeatureItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = OnBoarding2Colors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 텍스트 영역 (왼쪽)
            Column(
                modifier = Modifier.width(228.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = feature.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBoarding2Colors.OnSurface
                )

                Text(
                    text = feature.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = OnBoarding2Colors.Secondary,
                    lineHeight = 21.sp
                )
            }

            // 이미지 영역 (오른쪽) - 각 기능에 맞는 이미지 표시
            AsyncImage(
                model = feature.drawableRes ?: feature.imageUrl,  // drawable이 있으면 사용, 없으면 URL 사용
                contentDescription = feature.title,
                modifier = Modifier
                    .size(width = 130.dp, height = 66.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun PageIndicator2(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentPage)
                                OnBoarding2Colors.OnSurface
                            else
                                OnBoarding2Colors.Indicator,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun BottomSection(onNextClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(bottom = 20.dp)
    ) {
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OnBoarding2Colors.Primary,
                contentColor = OnBoarding2Colors.OnSurface
            )
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmOnBoarding2ScreenPreview() {
    WeCanFarmOnBoarding2Screen()
}