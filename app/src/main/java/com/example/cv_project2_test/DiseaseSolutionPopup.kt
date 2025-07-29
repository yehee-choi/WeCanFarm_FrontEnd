@file:OptIn(ExperimentalMaterial3Api::class)

// DiseaseSolutionPopup.kt
package com.example.cv_project2_test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// 질병 솔루션 데이터
data class DiseaseSolution(
    val name: String,
    val symptoms: List<String>,
    val causes: List<String>,
    val treatments: List<String>,
    val prevention: List<String>
)

// 고추 질병 솔루션 데이터베이스
object PepperSolutionDB {
    private val solutions = mapOf(
        "고추마일드모틀바이러스" to DiseaseSolution(
            name = "고추마일드모틀바이러스 (PMMoV)",
            symptoms = listOf(
                "잎에 연한 녹색과 짙은 녹색의 모자이크 무늬",
                "잎의 기형 및 주름 현상",
                "식물 성장 저해",
                "과실 크기 감소 및 기형",
                "과실 표면의 불규칙한 색깔 변화"
            ),
            causes = listOf(
                "진딧물을 통한 전염",
                "감염된 종자 사용",
                "농기구를 통한 기계적 전파",
                "작업자 손을 통한 접촉 전염"
            ),
            treatments = listOf(
                "감염된 식물 즉시 제거 및 소각 처리",
                "진딧물 방제용 살충제 살포 (이미다클로프리드 계열)",
                "토양 태양열 소독 또는 석회질소 처리",
                "건전한 무병 종자로 교체",
                "격리 재배 및 방충망 설치"
            ),
            prevention = listOf(
                "바이러스 무병 종자 사용",
                "진딧물 예방 방충망 설치",
                "작업 도구 70% 알코올 소독",
                "작업자 손 철저한 소독",
                "의심 식물 조기 발견 및 격리"
            )
        ),
        "고추점무늬병" to DiseaseSolution(
            name = "고추점무늬병 (Bacterial Spot)",
            symptoms = listOf(
                "잎에 작고 둥근 갈색 반점 형성",
                "반점 주위 노란 테두리(할로) 생성",
                "심한 경우 잎이 노랗게 변하고 낙엽",
                "줄기와 과실에 검은 반점 형성",
                "과실 표면 거친 코르크질 병반"
            ),
            causes = listOf(
                "Xanthomonas 세균 감염",
                "고온다습 환경 (25-30°C, 습도 85% 이상)",
                "과도한 질소 비료 사용",
                "통풍 불량한 재배 환경",
                "빗물이나 관수를 통한 전파"
            ),
            treatments = listOf(
                "구리 계열 살균제 살포 (황산구리, 수산화구리)",
                "항생제 계열 약제 사용 (스트렙토마이신)",
                "감염된 잎과 과실 즉시 제거",
                "토양 배수 시설 개선",
                "질소 비료 사용량 적정화"
            ),
            prevention = listOf(
                "저항성 품종 재배 선택",
                "적절한 재식거리 유지로 통풍 개선",
                "관수 시간 조절 (잎에 물방울 장시간 잔류 방지)",
                "토양 배수 시설 개선",
                "예방적 살균제 정기 살포"
            )
        )
    )

    fun getSolution(diseaseStatus: String): DiseaseSolution? {
        return solutions[diseaseStatus]
    }
}

// 진단 결과에 "더보기" 링크를 추가하는 컴포저블
@Composable
fun DiagnosisResultWithSolution(
    diseaseStatus: String, // 서버 응답의 disease_status 값
    accuracy: Float,
    onBackClick: () -> Unit = {}
) {
    var showSolutionPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 진단 결과 표시
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "진단 결과",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111614)
                )

                Text(
                    text = diseaseStatus,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53E3E)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "정확도:",
                        fontSize = 16.sp,
                        color = Color(0xFF111614)
                    )
                    Text(
                        text = "${(accuracy * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                // 더보기 링크
                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF2196F3),
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("치료법 및 예방법 자세히 보기")
                    }
                }

                ClickableText(
                    text = annotatedString,
                    onClick = { showSolutionPopup = true },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    // 솔루션 팝업
    if (showSolutionPopup) {
        DiseaseSolutionPopup(
            diseaseStatus = diseaseStatus,
            onDismiss = { showSolutionPopup = false }
        )
    }
}

// 솔루션 팝업 다이얼로그
@Composable
fun DiseaseSolutionPopup(
    diseaseStatus: String,
    onDismiss: () -> Unit
) {
    val solution = PepperSolutionDB.getSolution(diseaseStatus)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = solution?.name ?: diseaseStatus,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111614),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF111614)
                        )
                    }
                }

                Divider(color = Color(0xFFE5E7EB))

                // 내용
                if (solution != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 증상
                        item {
                            SolutionSection(
                                title = "🔍 주요 증상",
                                items = solution.symptoms,
                                backgroundColor = Color(0xFFF3F4F6)
                            )
                        }

                        // 원인
                        item {
                            SolutionSection(
                                title = "⚠️ 발생 원인",
                                items = solution.causes,
                                backgroundColor = Color(0xFFFEF3C7)
                            )
                        }

                        // 치료법
                        item {
                            SolutionSection(
                                title = "💊 치료 방법",
                                items = solution.treatments,
                                backgroundColor = Color(0xFFDCFCE7)
                            )
                        }

                        // 예방법
                        item {
                            SolutionSection(
                                title = "🛡️ 예방 방법",
                                items = solution.prevention,
                                backgroundColor = Color(0xFFDFE5FF)
                            )
                        }
                    }
                } else {
                    // 솔루션 데이터가 없는 경우
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "해당 질병에 대한 솔루션 정보가 없습니다.",
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolutionSection(
    title: String,
    items: List<String>,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111614)
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// 사용 예시
@Composable
fun ExampleUsage() {
    // 서버 응답에서 받은 데이터
    val diseaseStatus = "고추마일드모틀바이러스" // 서버 응답의 disease_status 값
    val accuracy = 0.92f

    DiagnosisResultWithSolution(
        diseaseStatus = diseaseStatus,
        accuracy = accuracy
    )
}