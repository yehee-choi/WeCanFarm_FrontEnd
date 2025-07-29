// WeCanFarmSignUpScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cv_project2_test

import android.util.Log
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
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

// 동일한 컬러 사용
object SignUpColors {
    val Primary = Color(0xFF6DCE6D)
    val Secondary = Color(0xFF6B826B)
    val Background = Color(0xFFF2F4F2)
    val Surface = Color.White
    val OnSurface = Color(0xFF111611)
    val Border = Color(0xFFDDE2DD)
}

// 회원가입 API 호출 함수 (작물 진단과 같은 서버 사용)
suspend fun registerUser(signUpData: SignUpData, serverUrl: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val fullUrl = "${serverUrl.trimEnd('/')}/api/auth/register"
            Log.d("SignUpAPI", "회원가입 API 호출: $fullUrl")

            val url = URL(fullUrl)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("ngrok-skip-browser-warning", "true") // ngrok 헤더 추가
            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            // SignUpData를 JSON으로 변환
            val gson = Gson()
            val jsonData = gson.toJson(signUpData)

            Log.d("SignUpAPI", "전송할 JSON 데이터: $jsonData")

            // JSON 데이터 전송
            connection.outputStream.use { outputStream ->
                outputStream.write(jsonData.toByteArray(Charsets.UTF_8))
            }

            Log.d("SignUpAPI", "데이터 전송 완료")

            val responseCode = connection.responseCode
            Log.d("SignUpAPI", "서버 응답 코드: $responseCode")

            val response = if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().readText()
                Log.d("SignUpAPI", "회원가입 성공: $responseText")
                "회원가입 성공: $responseText"
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "회원가입 실패: HTTP $responseCode"
                Log.e("SignUpAPI", "회원가입 실패: $errorText")
                "회원가입 실패: $errorText"
            }

            connection.disconnect()
            Log.d("SignUpAPI", "서버 연결 종료")

            response

        } catch (e: Exception) {
            Log.e("SignUpAPI", "회원가입 API 오류", e)
            "회원가입 실패: 네트워크 오류 - ${e.message}"
        }
    }
}
// 회원가입 응답 데이터 클래스
data class SignUpResponse(
    val message: String,
    val user_id: Int
)
data class SignUpData(
    val username: String,
    val email: String,
    val password: String,
    val full_name: String,
    val role: String // FARMER 또는 USER
)

@Composable
fun WeCanFarmSignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onBackToLoginClick: () -> Unit = {},
    serverUrl: String = "https://driven-sweeping-sheep.ngrok-free.app" // 작물 진단과 같은 서버
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("USER") } // 기본값은 USER
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // API 호출 상태
    var isLoading by remember { mutableStateOf(false) }
    var apiMessage by remember { mutableStateOf<String?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    // 회원가입 처리 함수
    fun handleSignUp() {
        if (password != confirmPassword) return
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || fullName.isEmpty()) return

        isLoading = true
        apiMessage = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val signUpData = SignUpData(
                    username = username,
                    email = email,
                    password = password,
                    full_name = fullName,
                    role = userType
                )

                val result = registerUser(signUpData, serverUrl)

                withContext(Dispatchers.Main) {
                    isLoading = false

                    if (result.contains("성공")) {
                        // 서버 응답 JSON 검증
                        val jsonResponse = result.substringAfter("회원가입 성공: ")

                        try {
                            // 지정된 형식으로 JSON 파싱 시도
                            val gson = Gson()
                            val signUpResponse = gson.fromJson(jsonResponse, SignUpResponse::class.java)

                            // 필수 필드가 있는지 확인
                            if (signUpResponse.message.isNotEmpty() && signUpResponse.user_id > 0) {
                                isSuccess = true
                                alertMessage = """
                                    🎉 ${signUpResponse.message}
                                    
                                    사용자 ID: ${signUpResponse.user_id}
                                    
                                    환영합니다! 이제 로그인하실 수 있습니다.
                                """.trimIndent()
                            } else {
                                // 필수 필드가 없거나 잘못된 값
                                isSuccess = false
                                alertMessage = "응답이 없습니다"
                            }

                        } catch (e: Exception) {
                            // JSON 파싱 실패
                            isSuccess = false
                            alertMessage = "응답이 없습니다"
                            Log.e("SignUpAPI", "JSON 파싱 실패: $jsonResponse", e)
                        }
                    } else {
                        isSuccess = false
                        alertMessage = result
                    }

                    showAlert = true
                    apiMessage = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isSuccess = false
                    alertMessage = "응답이 없습니다"
                    showAlert = true
                    apiMessage = "회원가입 실패: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignUpColors.Surface)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더 이미지 (로그인과 동일한 스타일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(SignUpColors.Primary)
        ) {
            Text(
                text = "🌱 회원가입 🌱",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 메인 타이틀
        Text(
            text = "WeCanFarm",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SignUpColors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 서브 타이틀
        Text(
            text = "새로운 농업 여정을 시작해보세요! 🚀",
            fontSize = 14.sp,
            color = SignUpColors.Secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 입력 필드들
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 사용자 유형 선택 (Radio Buttons)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "사용자 유형을 선택하세요",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SignUpColors.OnSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // USER 선택
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = userType == "USER",
                            onClick = { userType = "USER" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SignUpColors.Primary,
                                unselectedColor = SignUpColors.Secondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "👤 일반 사용자",
                            fontSize = 16.sp,
                            color = SignUpColors.OnSurface,
                            fontWeight = if (userType == "USER") FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    // FARMER 선택
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = userType == "FARMER",
                            onClick = { userType = "FARMER" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SignUpColors.Primary,
                                unselectedColor = SignUpColors.Secondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🌾 농부",
                            fontSize = 16.sp,
                            color = SignUpColors.OnSurface,
                            fontWeight = if (userType == "FARMER") FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            // 사용자명 입력
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = {
                    Text(
                        "사용자명을 입력하세요",
                        color = SignUpColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = SignUpColors.Background,
                    focusedContainerColor = SignUpColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = SignUpColors.Primary,
                    cursorColor = SignUpColors.Primary
                ),
                singleLine = true
            )

            // 전체 이름 입력
            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = {
                    Text(
                        "전체 이름을 입력하세요",
                        color = SignUpColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = SignUpColors.Background,
                    focusedContainerColor = SignUpColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = SignUpColors.Primary,
                    cursorColor = SignUpColors.Primary
                ),
                singleLine = true
            )

            // 이메일 입력
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "이메일을 입력하세요",
                        color = SignUpColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = SignUpColors.Background,
                    focusedContainerColor = SignUpColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = SignUpColors.Primary,
                    cursorColor = SignUpColors.Primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // 비밀번호 입력
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "비밀번호를 입력하세요",
                        color = SignUpColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = SignUpColors.Background,
                    focusedContainerColor = SignUpColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = SignUpColors.Primary,
                    cursorColor = SignUpColors.Primary
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

            // 비밀번호 확인 입력
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = {
                    Text(
                        "비밀번호를 다시 입력하세요",
                        color = SignUpColors.Secondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = SignUpColors.Background,
                    focusedContainerColor = SignUpColors.Background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword)
                        Color.Red else SignUpColors.Primary,
                    cursorColor = SignUpColors.Primary
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "🙈" else "👁️",
                            fontSize = 20.sp
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
            )

            // 비밀번호 불일치 경고
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text(
                    text = "⚠️ 비밀번호가 일치하지 않습니다",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // API 응답 메시지 표시
        apiMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.contains("성공"))
                        Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = if (message.contains("성공"))
                        Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 회원가입 버튼
        Button(
            onClick = { handleSignUp() },
            enabled = username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                    fullName.isNotEmpty() && password == confirmPassword && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignUpColors.Primary,
                contentColor = SignUpColors.OnSurface,
                disabledContainerColor = SignUpColors.Secondary,
                disabledContentColor = Color.White
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
                        text = "처리 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "회원가입",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 로그인으로 돌아가기 버튼
        OutlinedButton(
            onClick = onBackToLoginClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SignUpColors.Background,
                contentColor = SignUpColors.OnSurface
            ),
            border = null
        ) {
            Text(
                text = "이미 계정이 있으신가요? 로그인",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 약관 동의 섹션
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SignUpColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        SignUpColors.Border,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "회원가입 시 이용약관 및 개인정보처리방침에 동의하게 됩니다.",
                    fontSize = 12.sp,
                    color = SignUpColors.Secondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 혜택 안내
        Text(
            text = "✨ 가입하면 무료로 작물 건강 분석을 받을 수 있어요!",
            fontSize = 14.sp,
            color = SignUpColors.Primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Alert Dialog 표시
    if (showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
                // 성공 시에만 로그인 화면으로 이동
                if (isSuccess) {
                    onSignUpSuccess()
                }
            },
            title = {
                Text(
                    text = if (isSuccess) "🎉 회원가입 성공" else "❌ 회원가입 실패",
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
                        // 성공 시에만 로그인 화면으로 이동
                        if (isSuccess) {
                            onSignUpSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) SignUpColors.Primary else Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = if (isSuccess) "로그인 화면으로" else "확인",
                        color = Color.White
                    )
                }
            },
            dismissButton = if (!isSuccess) {
                {
                    TextButton(onClick = { showAlert = false }) {
                        Text("다시 시도", color = SignUpColors.Secondary)
                    }
                }
            } else null,
            containerColor = SignUpColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmSignUpScreenPreview() {
    WeCanFarmSignUpScreen()
}