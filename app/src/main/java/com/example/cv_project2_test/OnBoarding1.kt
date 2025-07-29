// OnBoarding.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// 온보딩 화면용 색상
object OnBoardingColors {
    val Primary = Color(0xFFD9D277)
    val Surface = Color.White
    val OnSurface = Color(0xFF161611)
    val Border = Color(0xFF000000)
    val Indicator = Color(0xFFE2E5DB)
}

// 온보딩 데이터 클래스
data class OnBoardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun WeCanFarmOnBoardingScreen(
    onNextClick: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    // 온보딩 페이지 데이터 (나중에 여러 페이지로 확장 가능)
    val pages = listOf(
        OnBoardingPage(
            title = "Welcome to WeCanFarm! 👋",
            subtitle = "Your farming journey starts here",
            description = "Whether you're growing on your balcony or backyard, we're here to help!",
            imageUrl = "https://placehold.co/388x320"
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, OnBoardingColors.Border, RoundedCornerShape(0.dp)),
        color = OnBoardingColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OnBoardingColors.Surface),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 컨텐츠 영역
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더 이미지
                AsyncImage(
                    model = R.drawable.onboarding1,
                    contentDescription = "OnBoarding Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 메인 타이틀
                Text(
                    text = page.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBoardingColors.OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 서브 타이틀
                Text(
                    text = page.subtitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBoardingColors.OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .height(55.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 설명 텍스트
                Text(
                    text = page.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = OnBoardingColors.OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 하단 네비게이션 영역
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // 페이지 인디케이터
                PageIndicator(
                    currentPage = currentPage,
                    totalPages = pages.size
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Next 버튼
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnBoardingColors.Primary,
                        contentColor = OnBoardingColors.OnSurface
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
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage)
                            OnBoardingColors.OnSurface
                        else
                            OnBoardingColors.Indicator,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        // 임시로 2개 인디케이터 표시 (확장 가능)
        if (totalPages == 1) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = OnBoardingColors.Indicator,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmOnBoardingScreenPreview() {
    WeCanFarmOnBoardingScreen()
}