package com.example.cv_project2_test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cv_project2_test.ui.theme.CV_Project2_TestTheme
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CV_Project2_TestTheme {
                // 현재 화면 상태 관리 (온보딩부터 시작)
                var currentScreen by remember { mutableStateOf("onboarding1") }

                when (currentScreen) {
                    // 온보딩 1 화면
                    "onboarding1" -> {
                        WeCanFarmOnBoardingScreen(
                            onNextClick = {
                                // 온보딩 2로 이동
                                currentScreen = "onboarding2"
                            }
                        )
                    }

                    // 온보딩 2 화면
                    "onboarding2" -> {
                        WeCanFarmOnBoarding2Screen(
                            onBackClick = {
                                // 온보딩 1으로 돌아가기
                                currentScreen = "onboarding1"
                            },
                            onNextClick = {
                                // 로그인 화면으로 이동
                                currentScreen = "login"
                            }
                        )
                    }

                    // 로그인 화면
                    "login" -> {
                        WeCanFarmLoginScreen(
                            onLoginSuccess = { userType ->
                                // 로그인 성공 시 사용자 역할에 따라 화면 이동
                                currentScreen = when (userType) {
                                    "FARMER" -> "farmer_dashboard"
                                    "USER" -> "market"
                                    else -> "market" // 기본값은 마켓으로
                                }
                            },
                            onSignUpClick = {
                                // 회원가입 화면으로 이동
                                currentScreen = "signup"
                            }
                        )
                    }

                    "signup" -> {
                        WeCanFarmSignUpScreen(
                            onSignUpSuccess = {
                                // 회원가입 성공 시 로그인 화면으로 돌아가기
                                currentScreen = "login"
                            },
                            onBackToLoginClick = {
                                // 로그인 화면으로 돌아가기
                                currentScreen = "login"
                            }
                        )
                    }

                    "plant_check" -> {
                        PlantCheckScreen(
                            onBackClick = {
                                // 농부는 대시보드로, 일반 사용자는 마켓으로
                                val userInfo = UserSession.getUserInfo()
                                currentScreen = when (userInfo?.user_type) {
                                    "FARMER" -> "farmer_dashboard"
                                    else -> "market"
                                }
                            }
                        )
                    }

                    "farmer_dashboard" -> {
                        val userInfo = UserSession.getUserInfo()
                        WeCanFarmFarmerScreen(
                            userName = userInfo?.full_name ?: "사용자",
                            userType = userInfo?.user_type ?: "FARMER",
                            onDiagnoseClick = {
                                currentScreen = "plant_check"
                            },
                            onMarketClick = {
                                currentScreen = "market"
                            }
                        )
                    }

                    "market" -> {
                        WeCanFarmMarketScreen(
                            onBackClick = {
                                // 사용자 역할에 따라 뒤로가기 화면 결정
                                val userInfo = UserSession.getUserInfo()
                                currentScreen = when (userInfo?.user_type) {
                                    "FARMER" -> "farmer_dashboard"
                                    else -> "login" // 일반 사용자는 로그인 화면으로
                                }
                            },
                            onNavigateToProductRegister = {
                                // 상품등록 화면으로 이동
                                currentScreen = "product_register"
                            },
                            onProductClick = { product ->
                                // 상품 상세 화면 (추후 구현)
                                println("상품 클릭: ${product.name}")
                                // currentScreen = "product_detail"
                            },
                            onCategoryClick = { category ->
                                // 카테고리별 상품 목록 (추후 구현)
                                println("카테고리 클릭: $category")
                            },
                            onBottomNavClick = { navItem ->
                                // 하단 네비게이션 처리
                                when (navItem) {
                                    "Home" -> {
                                        val userInfo = UserSession.getUserInfo()
                                        currentScreen = when (userInfo?.user_type) {
                                            "FARMER" -> "farmer_dashboard"
                                            else -> "market"
                                        }
                                    }
                                    "Diagnose" -> currentScreen = "plant_check"
                                    "Market" -> currentScreen = "market"
                                    "Community" -> println("커뮤니티 화면으로 이동")
                                    "Profile" -> println("프로필 화면으로 이동")
                                }
                            }
                        )
                    }

                    "product_register" -> {
                        WeCanFarmProductRegisterScreen(
                            onBackClick = {
                                // 마켓 화면으로 돌아가기
                                currentScreen = "market"
                            }
                        )
                    }
                }
            }
        }
    }
}

// 사용자 정보 데이터 클래스
data class UserInfo(
    val user_id: Int,
    val username: String,
    val full_name: String,
    val user_type: String
)

// 사용자 세션 관리 객체
object UserSession {
    private var accessToken: String? = null
    private var userInfo: UserInfo? = null

    // 로그인 정보 저장
    fun setLoginInfo(token: String, user: UserInfo) {
        accessToken = token
        userInfo = user
        Log.d("UserSession", "토큰 저장됨: ${token.take(20)}...")
        Log.d("UserSession", "사용자 정보 저장됨: ${user.full_name}")
    }

    // 토큰 조회
    fun getAccessToken(): String? {
        Log.d("UserSession", "토큰 조회: ${accessToken?.take(20) ?: "null"}")
        return accessToken
    }

    // 사용자 정보 조회
    fun getUserInfo(): UserInfo? {
        return userInfo
    }

    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        val loggedIn = !accessToken.isNullOrEmpty()
        Log.d("UserSession", "로그인 상태: $loggedIn")
        return loggedIn
    }

    // 세션 초기화
    fun clearSession() {
        accessToken = null
        userInfo = null
        Log.d("UserSession", "세션 초기화됨")
    }

    // 디버깅용 - 현재 세션 상태 출력
    fun debugSession() {
        Log.d("UserSession", "=== 세션 상태 ===")
        Log.d("UserSession", "토큰: ${accessToken ?: "없음"}")
        Log.d("UserSession", "사용자 정보: ${userInfo ?: "없음"}")
        Log.d("UserSession", "로그인 상태: ${isLoggedIn()}")
        Log.d("UserSession", "================")
    }
}

// 로그인 요청 데이터 클래스
data class LoginData(
    val username: String,
    val password: String
)

// 로그인 응답 데이터 클래스 (수정된 형식)
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int,
    val username: String,
    val role: String  // role 필드 추가
)

// 로그인 API 호출 함수
suspend fun loginUser(loginData: LoginData, serverUrl: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val fullUrl = "${serverUrl.trimEnd('/')}/api/auth/login"
            Log.d("LoginAPI", "로그인 API 호출: $fullUrl")

            val url = URL(fullUrl)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("ngrok-skip-browser-warning", "true")
            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            // LoginData를 JSON으로 변환
            val gson = Gson()
            val jsonData = gson.toJson(loginData)

            Log.d("LoginAPI", "전송할 JSON 데이터: $jsonData")

            // JSON 데이터 전송
            connection.outputStream.use { outputStream ->
                outputStream.write(jsonData.toByteArray(Charsets.UTF_8))
            }

            Log.d("LoginAPI", "데이터 전송 완료")

            val responseCode = connection.responseCode
            Log.d("LoginAPI", "서버 응답 코드: $responseCode")

            val response = if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().readText()
                Log.d("LoginAPI", "로그인 성공: $responseText")
                "로그인 성공: $responseText"
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "로그인 실패: HTTP $responseCode"
                Log.e("LoginAPI", "로그인 실패: $errorText")
                "로그인 실패: $errorText"
            }

            connection.disconnect()
            Log.d("LoginAPI", "서버 연결 종료")

            response

        } catch (e: Exception) {
            Log.e("LoginAPI", "로그인 API 오류", e)
            "로그인 실패: 네트워크 오류 - ${e.message}"
        }
    }
}

// 컬러 정의
object WeCanFarmColors {
    val Primary = Color(0xFF738903)
    val Secondary = Color(0xFF6B826B)
    val Background = Color(0xFFF2F4F2)
    val Surface = Color.White
    val OnSurface = Color(0xFF111611)
    val Border = Color(0xFFDDE2DD)
}

@Composable
fun WeCanFarmLoginScreen(
    onLoginSuccess: (String) -> Unit = {}, // String 파라미터 추가 (userType)
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    serverUrl: String = "https://driven-sweeping-sheep.ngrok-free.app"
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // API 호출 상태
    var isLoading by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var userType by remember { mutableStateOf("") } // 사용자 타입 저장

    // 로그인 처리 함수
    fun handleLogin() {
        if (username.isEmpty() || password.isEmpty()) return

        isLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginData = LoginData(
                    username = username,
                    password = password
                )

                val result = loginUser(loginData, serverUrl)

                withContext(Dispatchers.Main) {
                    isLoading = false

                    if (result.contains("성공")) {
                        // 서버 응답 JSON 검증
                        val jsonResponse = result.substringAfter("로그인 성공: ")

                        try {
                            // 지정된 형식으로 JSON 파싱 시도
                            val gson = Gson()
                            Log.d("DEBUG", "파싱할 JSON: $jsonResponse")
                            val loginResponse = gson.fromJson(jsonResponse, LoginResponse::class.java)

                            Log.d("DEBUG", "파싱된 username: '${loginResponse.username}'")
                            Log.d("DEBUG", "파싱된 user_id: ${loginResponse.user_id}")
                            Log.d("DEBUG", "파싱된 role: '${loginResponse.role}'")

                            // 필수 필드가 있는지 확인
                            if (loginResponse.access_token.isNotEmpty() &&
                                loginResponse.username.isNotEmpty() &&
                                loginResponse.user_id > 0) {

                                // 로그인 응답에서 직접 사용자 정보 생성
                                val userInfo = UserInfo(
                                    user_id = loginResponse.user_id,
                                    username = loginResponse.username,
                                    full_name = loginResponse.username,
                                    user_type = loginResponse.role
                                )

                                // 사용자 세션 정보 저장
                                UserSession.setLoginInfo(loginResponse.access_token, userInfo)

                                // 사용자 타입 저장
                                userType = loginResponse.role

                                isSuccess = true
                                alertMessage = """
                                    🎉 로그인 성공!
                                    
                                    환영합니다, ${userInfo.full_name}님!
                                    사용자 유형: ${if (userInfo.user_type == "FARMER") "🌾 농부" else "👤 일반사용자"}
                                    
                                    ${if (userInfo.user_type == "FARMER") "농장 관리를 시작하세요!" else "신선한 농산물을 만나보세요!"}
                                """.trimIndent()

                                Log.d("DEBUG", "사용자 정보 로드 완료: ${userInfo.full_name}, 유형: ${userInfo.user_type}")
                                showAlert = true
                            } else {
                                // 필수 필드가 없거나 잘못된 값
                                isSuccess = false
                                alertMessage = "응답이 없습니다"
                                showAlert = true
                                Log.d("DEBUG", "필드 검증 실패")
                            }

                        } catch (e: Exception) {
                            // JSON 파싱 실패
                            isSuccess = false
                            alertMessage = "응답이 없습니다"
                            showAlert = true
                            Log.e("DEBUG", "JSON 파싱 실패. 원본 응답: $jsonResponse", e)
                        }
                    } else {
                        isSuccess = false
                        alertMessage = result
                        showAlert = true
                        Log.d("DEBUG", "서버 응답이 '성공'을 포함하지 않음: $result")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isSuccess = false
                    alertMessage = "응답이 없습니다"
                    showAlert = true
                    Log.e("DEBUG", "전체 처리 과정에서 오류 발생", e)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WeCanFarmColors.Surface)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.main_logo),
                contentDescription = "WeCanFarm Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 메인 타이틀
        Text(
            text = "WeCanFarm",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = WeCanFarmColors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 서브 타이틀
        Text(
            text = "Your neighborhood farming buddy 🌱",
            fontSize = 14.sp,
            color = WeCanFarmColors.Secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 입력 필드들
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 사용자명 입력 (이메일에서 사용자명으로 변경)
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = {
                    Text(
                        "사용자명을 입력하세요",
                        color = WeCanFarmColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = WeCanFarmColors.Background,
                    focusedContainerColor = WeCanFarmColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = WeCanFarmColors.Primary,
                    cursorColor = WeCanFarmColors.Primary
                ),
                singleLine = true
            )

            // 비밀번호 입력
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "비밀번호를 입력하세요",
                        color = WeCanFarmColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = WeCanFarmColors.Background,
                    focusedContainerColor = WeCanFarmColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = WeCanFarmColors.Primary,
                    cursorColor = WeCanFarmColors.Primary
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "🙈" else "👁️",
                            fontSize = 20.sp
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 로그인 버튼
        Button(
            onClick = { handleLogin() },
            enabled = username.isNotEmpty() && password.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WeCanFarmColors.Primary,
                contentColor = WeCanFarmColors.OnSurface
            )
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "로그인 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "로그인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 회원가입 버튼
        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = WeCanFarmColors.Background,
                contentColor = WeCanFarmColors.OnSurface
            ),
            border = null
        ) {
            Text(
                text = "회원가입",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 찾기
        TextButton(
            onClick = onForgotPasswordClick
        ) {
            Text(
                text = "비밀번호를 잊으셨나요?",
                fontSize = 14.sp,
                color = WeCanFarmColors.Secondary
            )
        }
    }
    // Alert Dialog 표시 (수정됨)
    if (showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
                // 성공 시에만 다음 화면으로 이동 (사용자 타입 전달)
                if (isSuccess) {
                    onLoginSuccess(userType)
                }
            },
            title = {
                Text(
                    text = if (isSuccess) "🎉 로그인 성공" else "❌ 로그인 실패",
                    fontWeight = FontWeight.Bold,
                    color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    text = alertMessage,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlert = false
                        // 성공 시에만 다음 화면으로 이동 (사용자 타입 전달)
                        if (isSuccess) {
                            onLoginSuccess(userType)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) WeCanFarmColors.Primary else Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = if (isSuccess) {
                            if (userType == "FARMER") "농장 관리로" else "마켓으로"
                        } else "확인",
                        color = Color.White
                    )
                }
            },
            dismissButton = if (!isSuccess) {
                {
                    TextButton(onClick = { showAlert = false }) {
                        Text("다시 시도", color = WeCanFarmColors.Secondary)
                    }
                }
            } else null,
            containerColor = WeCanFarmColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmLoginScreenPreview() {
    CV_Project2_TestTheme {
        WeCanFarmLoginScreen()
    }
}