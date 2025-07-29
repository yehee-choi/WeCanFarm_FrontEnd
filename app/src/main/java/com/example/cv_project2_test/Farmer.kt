@file:OptIn(ExperimentalMaterial3Api::class)

// Farmer.kt - 농부 대시보드 (간소화된 작물 검사 기록 포함)
package com.example.cv_project2_test

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 컬러 정의
object HomeColors {
    val Primary = Color(0xFFD9D277)
    val Secondary = Color(0xFF738903)
    val Background = Color(0xFFF2F4F2)
    val Surface = Color.White
    val OnSurface = Color(0xFF111611)
    val Border = Color(0xFFDDE2DD)
}

// 간소화된 작물 검사 기록 데이터 클래스 (4가지 핵심 정보만)
data class PlantDetectionRecord(
    val id: String = "",
    val userId: Int = 0,
    val timestamp: String = "", // 촬영 날짜
    val cropType: String = "",  // 작물 종류
    val diseaseStatus: String = "", // 병해 상태
    val imageBase64: String = "" // 촬영한 이미지
)

// 농부 대시보드용 데이터 클래스들
data class CropData(
    val cropName: String,
    val plantedDate: String,
    val currentStage: String,
    val healthStatus: String,
    val expectedHarvest: String,
    val imageUrl: String = "https://placehold.co/120x120"
)

data class DashboardStat(
    val title: String,
    val value: String,
    val subtitle: String = "",
    val color: Color = HomeColors.Primary
)

data class RecentActivity(
    val action: String,
    val crop: String,
    val date: String,
    val icon: ImageVector
)

// 검사 기록 저장소 객체 (간소화)
object PlantDetectionHistory {
    private val detectionRecords = mutableStateListOf<PlantDetectionRecord>()

    // 새로운 검사 기록 추가 (4가지 핵심 정보만)
    fun addDetection(
        userId: Int,
        detectionResponse: DetectionResponse,
        capturedImageBitmap: Bitmap?
    ) {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

            // 이미지를 Base64로 변환
            var imageBase64 = ""
            capturedImageBitmap?.let { bitmap ->
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            }

            // 검출된 각 항목에 대해 간단한 기록 생성
            detectionResponse.detections.forEach { detection ->
                val record = PlantDetectionRecord(
                    id = "${userId}_${System.currentTimeMillis()}_${detection.hashCode()}",
                    userId = userId,
                    timestamp = timestamp,
                    cropType = detection.crop_type,
                    diseaseStatus = detection.disease_status,
                    imageBase64 = imageBase64
                )

                detectionRecords.add(0, record) // 최신 기록을 맨 앞에 추가
                Log.d("PlantDetectionHistory", "새 검사 기록 추가: ${detection.crop_type} - ${detection.disease_status}")
            }

            // 최대 50개 기록만 유지
            if (detectionRecords.size > 50) {
                detectionRecords.removeRange(50, detectionRecords.size)
            }

        } catch (e: Exception) {
            Log.e("PlantDetectionHistory", "검사 기록 저장 실패", e)
        }
    }

    // 사용자별 검사 기록 조회
    fun getDetectionsByUser(userId: Int): List<PlantDetectionRecord> {
        return detectionRecords.filter { it.userId == userId }
    }

    // 모든 검사 기록 조회
    fun getAllDetections(): List<PlantDetectionRecord> {
        return detectionRecords.toList()
    }

    // 건강한 검사 수
    fun getHealthyDetectionsCount(userId: Int): Int {
        return detectionRecords.count {
            it.userId == userId && it.diseaseStatus.contains("healthy", ignoreCase = true)
        }
    }

    // 불건전한 검사 수
    fun getUnhealthyDetectionsCount(userId: Int): Int {
        return detectionRecords.count {
            it.userId == userId && !it.diseaseStatus.contains("healthy", ignoreCase = true)
        }
    }
}

@Composable
fun WeCanFarmFarmerScreen(
    userName: String = "사용자",
    userType: String = "USER", // FARMER 또는 USER
    onDiagnoseClick: () -> Unit = {},
    onMarketClick: () -> Unit = {}
) {
    val userInfo = UserSession.getUserInfo()
    val currentUserId = userInfo?.user_id ?: 0

    // 실제 검사 기록 가져오기
    val detectionRecords by remember {
        derivedStateOf { PlantDetectionHistory.getDetectionsByUser(currentUserId) }
    }

    // 건강한/불건전한 검사 수 계산
    val healthyCount = PlantDetectionHistory.getHealthyDetectionsCount(currentUserId)
    val unhealthyCount = PlantDetectionHistory.getUnhealthyDetectionsCount(currentUserId)
    val totalDetections = detectionRecords.size

    // 샘플 작물 데이터
    val cropData = listOf(
        CropData(
            cropName = "방울토마토",
            plantedDate = "2024.03.15",
            currentStage = "개화기",
            healthStatus = "건강",
            expectedHarvest = "2024.07.20"
        ),
        CropData(
            cropName = "상추",
            plantedDate = "2024.04.01",
            currentStage = "수확기",
            healthStatus = "양호",
            expectedHarvest = "2024.06.01"
        ),
        CropData(
            cropName = "오이",
            plantedDate = "2024.04.10",
            currentStage = "생장기",
            healthStatus = if (unhealthyCount > 0) "주의" else "건강",
            expectedHarvest = "2024.08.15"
        )
    )

    // 동적 대시보드 통계 (실제 검사 데이터 반영)
    val dashboardStats = listOf(
        DashboardStat("총 작물 수", "8", "재배중"),
        DashboardStat("AI 진단 횟수", "$totalDetections", "누적"),
        DashboardStat("건강한 작물", "$healthyCount", "진단 결과"),
        DashboardStat("주의 필요", "$unhealthyCount", "진단 결과")
    )

    // 검사 기록을 최근 활동으로 변환
    val recentActivitiesFromDetections = detectionRecords.take(3).map { record ->
        RecentActivity(
            action = "AI 진단 완료",
            crop = "${record.cropType} - ${record.diseaseStatus}",
            date = record.timestamp,
            icon = if (record.diseaseStatus.contains("healthy", ignoreCase = true))
                Icons.Default.CheckCircle else Icons.Default.Warning
        )
    }

    // 기본 활동과 검사 기록 활동 결합
    val defaultActivities = listOf(
        RecentActivity("물주기 완료", "방울토마토", "오늘 오전 8:00", Icons.Default.WaterDrop),
        RecentActivity("수확 완료", "상추", "2일 전", Icons.Default.Agriculture)
    )

    val allActivities = (recentActivitiesFromDetections + defaultActivities).take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeColors.Surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 헤더 - 사용자 이름
        item {
            FarmerHeaderSection(userName, userType)
        }

        // 진단하기, 마켓이동 버튼
        item {
            ActionButtonsSection(onDiagnoseClick, onMarketClick)
        }

        // 대시보드 통계
        item {
            SectionTitle("농장 현황")
            DashboardStatsSection(dashboardStats)
        }

        // AI 진단 기록 섹션 (간소화된 버전)
        if (detectionRecords.isNotEmpty()) {
            item {
                SectionTitle("최근 AI 진단 기록")
            }

            items(detectionRecords.take(5)) { record ->
                SimplePlantDetectionCard(record)
            }

            if (detectionRecords.size > 5) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HomeColors.Primary.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "더 많은 진단 기록 보기 (+${detectionRecords.size - 5}개)",
                                color = HomeColors.Secondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 내 작물 현황
        item {
            SectionTitle("내 작물 현황")
        }

        items(cropData.size) { index ->
            CropStatusCard(cropData[index])
        }

        // 최근 활동
        item {
            SectionTitle("최근 활동")
        }

        items(allActivities.size) { index ->
            RecentActivityItem(allActivities[index])
        }
    }
}

// 간소화된 검사 기록 카드 (4가지 핵심 정보만 표시)
@Composable
fun SimplePlantDetectionCard(record: PlantDetectionRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HomeColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 촬영한 이미지
            if (record.imageBase64.isNotEmpty()) {
                val imageBytes = runCatching { Base64.decode(record.imageBase64, Base64.DEFAULT) }.getOrNull()
                imageBytes?.let { bytes ->
                    val bitmap = runCatching {
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }.getOrNull()
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "검사한 작물 이미지",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // 이미지가 없을 때 기본 아이콘
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = HomeColors.Primary.copy(alpha = 0.1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Grass,
                            contentDescription = "작물 아이콘",
                            tint = HomeColors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // 작물 정보 (3가지 핵심 정보)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 작물 종류
                Text(
                    text = record.cropType,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.OnSurface
                )

                // 촬영 날짜
                Text(
                    text = "📅 ${record.timestamp}",
                    fontSize = 14.sp,
                    color = HomeColors.Secondary
                )

                // 병해 상태 (색상으로 구분)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "상태:",
                        fontSize = 14.sp,
                        color = HomeColors.Secondary
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (record.diseaseStatus.contains("healthy", ignoreCase = true))
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            Color(0xFFFF9800).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = record.diseaseStatus,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (record.diseaseStatus.contains("healthy", ignoreCase = true))
                                Color(0xFF4CAF50)
                            else
                                Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerHeaderSection(userName: String, userType: String = "USER") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${userName}님, 안녕하세요! ${if (userType == "FARMER") "🌾" else "👤"}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = HomeColors.OnSurface,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { /* TODO: 프로필 메뉴 */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = HomeColors.Secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ActionButtonsSection(
    onDiagnoseClick: () -> Unit,
    onMarketClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 진단하기 버튼
        Button(
            onClick = onDiagnoseClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeColors.Primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "진단하기",
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "🔍 작물 진단하기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI로 작물 건강상태를 확인하세요",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 마켓이동 버튼
        Button(
            onClick = onMarketClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeColors.Secondary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "마켓이동",
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "🛒 농산물 마켓",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "신선한 농산물을 판매하세요",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStatsSection(stats: List<DashboardStat>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 첫 번째 줄 (2개)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { index ->
                DashboardStatCard(
                    stat = stats[index],
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 두 번째 줄 (2개)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) { index ->
                DashboardStatCard(
                    stat = stats[index + 2],
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    stat: DashboardStat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HomeColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, HomeColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stat.title,
                fontSize = 14.sp,
                color = HomeColors.Secondary
            )
            Text(
                text = stat.value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HomeColors.OnSurface
            )
            if (stat.subtitle.isNotEmpty()) {
                Text(
                    text = stat.subtitle,
                    fontSize = 12.sp,
                    color = HomeColors.Secondary
                )
            }
        }
    }
}

@Composable
fun CropStatusCard(crop: CropData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HomeColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 작물 이미지
            AsyncImage(
                model = crop.imageUrl,
                contentDescription = crop.cropName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // 작물 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = crop.cropName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeColors.OnSurface
                )

                Text(
                    text = "심은 날짜: ${crop.plantedDate}",
                    fontSize = 14.sp,
                    color = HomeColors.Secondary
                )

                Text(
                    text = "현재 단계: ${crop.currentStage}",
                    fontSize = 14.sp,
                    color = HomeColors.Secondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "건강상태:",
                        fontSize = 14.sp,
                        color = HomeColors.Secondary
                    )
                    Text(
                        text = crop.healthStatus,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (crop.healthStatus) {
                            "건강" -> Color(0xFF4CAF50)
                            "양호" -> Color(0xFF2196F3)
                            "주의" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                Text(
                    text = "수확 예정: ${crop.expectedHarvest}",
                    fontSize = 14.sp,
                    color = HomeColors.Secondary
                )
            }
        }
    }
}

@Composable
fun RecentActivityItem(activity: RecentActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = HomeColors.Primary.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = activity.action,
                    tint = HomeColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 활동 정보
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${activity.action} - ${activity.crop}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = HomeColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = activity.date,
                fontSize = 14.sp,
                color = HomeColors.Secondary
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = HomeColors.OnSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmFarmerScreenPreview() {
    WeCanFarmFarmerScreen()
}