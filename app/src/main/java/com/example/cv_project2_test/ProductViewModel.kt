package com.example.cv_project2_test
// ProductViewModel.kt

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 공통 상품 데이터 클래스 (Market과 Register 통합)
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // cropType을 name으로 사용
    val seller: String,
    val distance: String = "0.1km", // 기본값
    val imageUri: Uri? = null, // 실제 촬영된 이미지
    val imageUrl: String = "", // 기본 placeholder
    val drawableRes: Int? = null,
    val price: String,
    val unit: String,
    val quantity: String,
    val harvestDate: String,
    val isOrganic: Boolean,
    val pickupLocation: String,
    val description: String,
    val registeredDate: String = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
)

class ProductViewModel : ViewModel() {
    // 등록된 상품들을 저장하는 리스트
    private val _registeredProducts = mutableStateListOf<Product>()
    val registeredProducts: List<Product> get() = _registeredProducts

    // 등록 상태 관리
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    // 기본 샘플 데이터 (앱 시작시 표시할 상품들)
    private val defaultProducts = listOf(
        Product(
            name = "방울토마토",
            seller = "김농부",
            distance = "0.5km",
            imageUrl = "https://placehold.co/240x320",
            drawableRes = R.drawable.tomato,
            price = "15000",
            unit = "kg",
            quantity = "10",
            harvestDate = "2024.07.20",
            isOrganic = true,
            pickupLocation = "서울시 강남구 농장",
            description = "신선한 방울토마토입니다. 유기농으로 재배했습니다."
        ),
        Product(
            name = "바질",
            seller = "이농부",
            distance = "1.2km",
            imageUrl = "https://placehold.co/240x320",
            drawableRes = R.drawable.basil1,
            price = "8000",
            unit = "묶음",
            quantity = "20",
            harvestDate = "2024.07.25",
            isOrganic = false,
            pickupLocation = "서울시 서초구 허브농장",
            description = "향긋한 바질입니다. 요리에 활용하세요."
        )
    )

    init {
        // 앱 시작시 기본 상품들 추가
        _registeredProducts.addAll(defaultProducts)
    }

    // 상품 등록 함수
    fun registerProduct(productRegistration: ProductRegistration, sellerName: String = "내 농장") {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading

            try {
                // ProductRegistration을 Product로 변환
                val product = Product(
                    name = productRegistration.cropType,
                    seller = sellerName,
                    imageUri = productRegistration.images.firstOrNull(), // 첫 번째 이미지 사용
                    price = productRegistration.price,
                    unit = productRegistration.unit,
                    quantity = productRegistration.quantity,
                    harvestDate = productRegistration.harvestDate,
                    isOrganic = productRegistration.isOrganic,
                    pickupLocation = productRegistration.pickupLocation,
                    description = productRegistration.description
                )

                // 등록된 상품을 리스트 맨 앞에 추가 (최신 등록 상품이 먼저 보이도록)
                _registeredProducts.add(0, product)

                _registrationState.value = RegistrationState.Success
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error(e.message ?: "등록 중 오류가 발생했습니다.")
            }
        }
    }

    // 상태 초기화
    fun resetRegistrationState() {
        _registrationState.value = RegistrationState.Idle
    }

    // Market 화면용 상품 리스트 반환 (MarketProduct 형태로 변환)
    fun getMarketProducts(): List<MarketProduct> {
        return _registeredProducts.map { product ->
            MarketProduct(
                name = product.name + if (product.isOrganic) " (유기농)" else "",
                seller = product.seller,
                distance = product.distance,
                imageUrl = product.imageUrl,
                drawableRes = product.drawableRes
            )
        }
    }

    // 추천 상품들 (등록된 상품 중 최신 3개)
    fun getFeaturedProducts(): List<MarketProduct> {
        return getMarketProducts().take(3)
    }

    // 인기 상품들 (나머지 상품들)
    fun getPopularProducts(): List<MarketProduct> {
        return getMarketProducts().drop(3)
    }
}

// 등록 상태 관리
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}