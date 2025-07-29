// PlantCheck.kt (카메라 회전 수정된 버전)
package com.example.cv_project2_test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.util.Base64
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

//데이터 파싱 : 아래와 같은 json파일 형태로 구성
data class Detection(
    val bbox: List<Int>,
    val crop_type: String,
    val disease_status: String,
    val disease_confidence: Double,
    val yolo_confidence: Double,
    val label: String
)

data class DetectionResponse(
    val image_base64: String,
    val total_detections: Int,
    val detections: List<Detection>
)

// 이미지 분석 요청 데이터 클래스 (API 스펙에 맞게)
data class ImageAnalysisRequest(
    val image_base64: String
)

// 수정된 이미지 업로드 함수 - Authorization 헤더와 올바른 JSON 형식 사용
suspend fun uploadImageAsBase64(bitmap: Bitmap, accessToken: String, serverUrl: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val fullUrl = "${serverUrl.trimEnd('/')}/api/analyze"
            Log.d("ImageUpload", "서버 접속 시도: $fullUrl")

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            Log.d("ImageUpload", "이미지 Base64 인코딩 완료, 크기: ${base64String.length}")

            val url = URL(fullUrl)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Authorization", "Bearer $accessToken") // 올바른 Authorization 헤더
            connection.setRequestProperty("ngrok-skip-browser-warning", "true")
            connection.setRequestProperty("Accept", "application/json")

            // 헤더 디버깅 - 실제 전송되는 헤더 확인
            Log.d("ImageUpload", "=== HTTP 헤더 디버깅 ===")
            Log.d("ImageUpload", "Content-Type: ${connection.getRequestProperty("Content-Type")}")
            Log.d("ImageUpload", "Authorization: ${connection.getRequestProperty("Authorization")}")
            Log.d("ImageUpload", "ngrok-skip-browser-warning: ${connection.getRequestProperty("ngrok-skip-browser-warning")}")
            Log.d("ImageUpload", "Accept: ${connection.getRequestProperty("Accept")}")
            Log.d("ImageUpload", "전체 토큰: $accessToken")
            Log.d("ImageUpload", "토큰 앞 20자: ${accessToken.take(20)}")
            Log.d("ImageUpload", "========================")
            connection.connectTimeout = 30000  // 타임아웃 증가
            connection.readTimeout = 60000

            Log.d("ImageUpload", "서버 연결 설정 완료")
            Log.d("ImageUpload", "Authorization 헤더: Bearer ${accessToken.take(20)}...")

            // API 스펙에 맞는 JSON 형식 사용
            val analysisRequest = ImageAnalysisRequest(image_base64 = base64String)
            val gson = Gson()
            val jsonData = gson.toJson(analysisRequest)

            Log.d("ImageUpload", "JSON 데이터 생성 완료, 크기: ${jsonData.length}")

            // JSON 데이터 전송
            val jsonBytes = jsonData.toByteArray(Charsets.UTF_8)
            connection.setRequestProperty("Content-Length", jsonBytes.size.toString())

            connection.outputStream.use { outputStream ->
                outputStream.write(jsonBytes)
                outputStream.flush()
            }

            Log.d("ImageUpload", "이미지 데이터 전송 완료")

            val responseCode = connection.responseCode
            Log.d("ImageUpload", "서버 응답 코드: $responseCode")

            val response = if (responseCode in 200..299) {
                val responseText = connection.inputStream.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                }
                Log.d("ImageUpload", "서버 응답 성공: $responseText")
                "업로드 성공: $responseText"
            } else {
                val errorText = connection.errorStream?.use { errorStream ->
                    errorStream.bufferedReader(Charsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                } ?: "업로드 실패: HTTP $responseCode"
                Log.e("ImageUpload", "서버 응답 실패: $errorText")
                "업로드 실패: $errorText"
            }

            connection.disconnect()
            Log.d("ImageUpload", "서버 연결 종료")

            response

        } catch (e: Exception) {
            Log.e("ImageUpload", "이미지 업로드 에러", e)
            "업로드 실패: 네트워크 오류 - ${e.message}"
        }
    }
}

@Suppress("DEPRECATION")
class CameraPreview(
    context: Context,
    private val onPictureTaken: (Bitmap) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private var camera: Camera? = null
    private val holder: SurfaceHolder = getHolder().apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera = Camera.open().apply {
                setPreviewDisplay(holder)
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "카메라 열기 실패", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (holder.surface == null) return

        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            // 무시
        }

        try {
            camera?.apply {
                val parameters = getParameters()
                val sizes = parameters.supportedPreviewSizes
                val bestSize = sizes.minByOrNull {
                    Math.abs(it.width * it.height - width * height)
                }
                bestSize?.let {
                    parameters.setPreviewSize(it.width, it.height)
                    setParameters(parameters)
                }

                // 카메라 방향을 올바르게 설정 (회전 문제 해결)
                setDisplayOrientation(0) // 0도로 설정하여 자연스러운 방향 유지

                setPreviewDisplay(holder)
                startPreview()
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "카메라 설정 실패", e)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    fun takePicture() {
        camera?.takePicture(null, null) { data, _ ->
            try {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                // 회전을 제거하여 자연스러운 방향 유지
                // val rotatedBitmap = rotateBitmap(bitmap, 90f) // 기존 회전 코드 제거
                onPictureTaken(bitmap) // 원본 이미지 그대로 사용
            } catch (e: Exception) {
                Log.e("CameraPreview", "사진 처리 실패", e)
            }
        }
    }

    private fun releaseCamera() {
        camera?.apply {
            stopPreview()
            release()
        }
        camera = null
    }

    // 필요한 경우에만 사용할 수 있도록 회전 함수는 유지 (현재는 사용하지 않음)
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

@Composable
fun PlantCheckScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadResult by remember { mutableStateOf<String?>(null) }
    var serverResponse by remember { mutableStateOf<DetectionResponse?>(null) }
    var cameraPreview by remember { mutableStateOf<CameraPreview?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    // 서버 URL을 코드에서 직접 설정
    val serverUrl = "https://driven-sweeping-sheep.ngrok-free.app"

    // 카메라 권한 확인
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // 카메라 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 화면 진입 시 권한 확인 및 요청
    LaunchedEffect(Unit) {
        if (hasCameraPermission) {
            hasPermission = true
        } else if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun uploadImage(bitmap: Bitmap) {
        if (isUploading) return

        // 토큰 확인
        val accessToken = UserSession.getAccessToken()
        Log.d("PlantCheck", "=== 토큰 디버깅 ===")
        Log.d("PlantCheck", "가져온 토큰: $accessToken")
        Log.d("PlantCheck", "토큰 길이: ${accessToken?.length ?: 0}")
        Log.d("PlantCheck", "토큰이 null인가: ${accessToken == null}")
        Log.d("PlantCheck", "토큰이 비어있는가: ${accessToken?.isEmpty() ?: true}")
        Log.d("PlantCheck", "=====================")

        if (accessToken.isNullOrEmpty()) {
            alertMessage = "로그인이 필요합니다. 다시 로그인해주세요."
            showAlert = true
            Log.e("PlantCheck", "토큰이 없음 - 로그인 필요")
            return
        }

        isUploading = true
        uploadResult = null
        serverResponse = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = uploadImageAsBase64(bitmap, accessToken!!, serverUrl)

                withContext(Dispatchers.Main) {
                    if (result.startsWith("업로드 성공:")) {
                        try {
                            val responseJson = result.substringAfter("업로드 성공: ")
                            val parsedResponse = Gson().fromJson(responseJson, DetectionResponse::class.java)

                            uploadResult = result
                            serverResponse = parsedResponse

                            // 🎯 검사 기록을 히스토리에 저장
                            val userInfo = UserSession.getUserInfo()
                            if (userInfo != null && capturedImage != null) {
                                PlantDetectionHistory.addDetection(
                                    userId = userInfo.user_id,
                                    detectionResponse = parsedResponse,
                                    capturedImageBitmap = capturedImage
                                )
                            }

                            if (parsedResponse.total_detections > 0) {
                                alertMessage = "🎉 분석 완료!\n총 ${parsedResponse.total_detections}개의 작물을 감지했습니다."
                            } else {
                                alertMessage = "📷 이미지를 분석했지만 작물을 감지하지 못했습니다.\n다른 각도에서 다시 촬영해보세요."
                            }
                            showAlert = true

                        } catch (e: Exception) {
                            Log.e("PlantCheck", "JSON 파싱 실패", e)
                            uploadResult = result
                            alertMessage = "서버 응답을 처리하는 중 오류가 발생했습니다."
                            showAlert = true
                        }
                    } else {
                        uploadResult = result
                        alertMessage = if (result.contains("401") || result.contains("Unauthorized")) {
                            "인증이 만료되었습니다. 다시 로그인해주세요."
                        } else {
                            "이미지 분석에 실패했습니다.\n네트워크를 확인하고 다시 시도해주세요."
                        }
                        showAlert = true
                    }
                    isUploading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uploadResult = "업로드 실패: ${e.message}"
                    alertMessage = "네트워크 오류가 발생했습니다.\n인터넷 연결을 확인해주세요."
                    showAlert = true
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단에 헤더와 뒤로가기 버튼
        PlantCheckHeader(onBackClick = onBackClick)

        PlantCheckTitle()

        // 권한이 있으면 바로 카메라 표시, 없으면 권한 요청 메시지
        if (hasPermission) {
            // 카메라 프리뷰와 촬영 버튼을 바로 표시
            CameraPreviewSection(
                context = context,
                onPictureTaken = { bitmap ->
                    capturedImage = bitmap
                    uploadImage(bitmap)
                },
                onCameraPreviewCreated = { preview ->
                    cameraPreview = preview
                }
            )
        } else {
            // 권한이 없을 때 안내 메시지
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷 카메라 권한 필요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "작물을 촬영하고 분석하기 위해\n카메라 권한이 필요합니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("권한 허용하기")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 촬영된 이미지 표시
        capturedImage?.let { bitmap ->
            CapturedImageView(bitmap)
        }

        // 업로드 상태 표시
        if (isUploading) {
            UploadingIndicator()
        }

        // 서버 응답 결과 표시
        serverResponse?.let { response ->
            DetectionResultView(response)
        }
    }

    // Alert Dialog 표시
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = {
                Text(
                    text = if (serverResponse != null && serverResponse!!.total_detections > 0)
                        "🎉 분석 완료" else "📷 분석 결과",
                    fontWeight = FontWeight.Bold
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
                    onClick = { showAlert = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("확인", color = Color.White)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CameraPreviewSection(
    context: Context,
    onPictureTaken: (Bitmap) -> Unit,
    onCameraPreviewCreated: (CameraPreview) -> Unit
) {
    var cameraPreview: CameraPreview? by remember { mutableStateOf(null) }

    Column {
        // 카메라 프리뷰 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    CameraPreview(ctx) { bitmap ->
                        onPictureTaken(bitmap)
                    }.also { preview ->
                        cameraPreview = preview
                        onCameraPreviewCreated(preview)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 촬영 버튼
        Button(
            onClick = {
                cameraPreview?.takePicture()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // 파란색 배경
                contentColor = Color.White          // 흰색 텍스트
            )
        ) {
            Text(
                text = "📸 촬영 & AI 분석",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UploadingIndicator() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "🔍 이미지 분석 중...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "AI가 작물을 분석하고 있습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun PlantCheckHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        // 제목
        Text(
            text = "작물 진단",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // 오른쪽 여백 (대칭을 위해)
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun PlantCheckTitle() {
    Text(
        text = "🌱 AI 작물 분석",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "작물을 촬영하면 AI가 자동으로 분석합니다",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun CapturedImageView(bitmap: Bitmap) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📷 촬영된 이미지",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "촬영된 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun DetectionResultView(response: DetectionResponse) {
    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 분석 결과",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "총 감지 개수: ${response.total_detections}개",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            response.detections.forEachIndexed { index, detection ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📋 감지 항목 ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("🏷️ 라벨: ${detection.label}")
                        Text("🌿 작물 종류: ${detection.crop_type}")
                        Text("🦠 병해 상태: ${detection.disease_status}")
                        Text("📈 질병 신뢰도: ${String.format("%.1f%%", detection.disease_confidence * 100)}")
                        Text("🎯 YOLO 신뢰도: ${String.format("%.1f%%", detection.yolo_confidence * 100)}")
                        Text("📦 위치: [${detection.bbox.joinToString(", ")}]")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantCheckScreenPreview() {
    PlantCheckScreen()
}