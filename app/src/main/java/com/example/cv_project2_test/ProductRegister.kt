
@file:OptIn(ExperimentalMaterial3Api::class)

// ProductRegister.kt
package com.example.cv_project2_test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// 상품 등록 화면용 색상
object RegisterColors {
    val Primary = Color(0xFF11EA68)
    val Surface = Color.White
    val OnSurface = Color(0xFF111614)
    val Secondary = Color(0xFF608970)
    val Background = Color(0xFFF8FAF9)
    val Border = Color(0xFFE5E7EB)
    val ImagePlaceholder = Color(0xFFF3F4F6)
}

// 데이터 클래스들
data class ProductRegistration(
    val images: List<String> = emptyList(),
    val cropType: String = "",
    val price: String = "",
    val unit: String = "kg",
    val description: String = "",
    val quantity: String = "",
    val harvestDate: String = "",
    val isOrganic: Boolean = false,
    val pickupLocation: String = ""
)

data class CropCategory(
    val name: String,
    val emoji: String,
    val isSelected: Boolean = false
)

@Composable
fun WeCanFarmProductRegisterScreen(
    onBackClick: () -> Unit = {},
    onImageAddClick: () -> Unit = {},
    onRegisterClick: (ProductRegistration) -> Unit = {}
) {
    var productData by remember { mutableStateOf(ProductRegistration()) }

    val cropCategories = listOf(
        CropCategory("토마토", "🍅"),
        CropCategory("상추", "🥬"),
        CropCategory("오이", "🥒"),
        CropCategory("당근", "🥕"),
        CropCategory("감자", "🥔"),
        CropCategory("양파", "🧅"),
        CropCategory("배추", "🥗"),
        CropCategory("기타", "🌿")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterColors.Background),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 헤더
        item {
            RegisterHeaderSection(onBackClick = onBackClick)
        }

        // 이미지 등록 섹션
        item {
            ImageRegistrationSection(
                images = productData.images,
                onImageAddClick = onImageAddClick,
                onImageRemove = { index ->
                    productData = productData.copy(
                        images = productData.images.toMutableList().apply { removeAt(index) }
                    )
                }
            )
        }

        // 작물 종류 선택
        item {
            CropTypeSection(
                categories = cropCategories,
                selectedCrop = productData.cropType,
                onCropSelect = { cropName ->
                    productData = productData.copy(cropType = cropName)
                }
            )
        }

        // 가격 및 단위
        item {
            PriceSection(
                price = productData.price,
                unit = productData.unit,
                onPriceChange = { price ->
                    productData = productData.copy(price = price)
                },
                onUnitChange = { unit ->
                    productData = productData.copy(unit = unit)
                }
            )
        }

        // 수량
        item {
            QuantitySection(
                quantity = productData.quantity,
                onQuantityChange = { quantity ->
                    productData = productData.copy(quantity = quantity)
                }
            )
        }

        // 수확 날짜
        item {
            HarvestDateSection(
                harvestDate = productData.harvestDate,
                onDateChange = { date ->
                    productData = productData.copy(harvestDate = date)
                }
            )
        }

        // 유기농 여부
        item {
            OrganicSection(
                isOrganic = productData.isOrganic,
                onOrganicChange = { isOrganic ->
                    productData = productData.copy(isOrganic = isOrganic)
                }
            )
        }

        // 픽업 장소
        item {
            PickupLocationSection(
                location = productData.pickupLocation,
                onLocationChange = { location ->
                    productData = productData.copy(pickupLocation = location)
                }
            )
        }

        // 설명
        item {
            DescriptionSection(
                description = productData.description,
                onDescriptionChange = { description ->
                    productData = productData.copy(description = description)
                }
            )
        }

        // 등록 버튼
        item {
            RegisterButtonSection(
                onRegisterClick = { onRegisterClick(productData) },
                isValid = isFormValid(productData)
            )
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RegisterHeaderSection(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RegisterColors.Surface)
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
                tint = RegisterColors.OnSurface
            )
        }

        // 제목
        Text(
            text = "농산물 등록",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RegisterColors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // 여백 (대칭을 위해)
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun ImageRegistrationSection(
    images: List<String>,
    onImageAddClick: () -> Unit,
    onImageRemove: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RegisterColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "상품 사진",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RegisterColors.OnSurface
        )

        Text(
            text = "최대 5장까지 등록 가능합니다",
            fontSize = 14.sp,
            color = RegisterColors.Secondary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이미지 추가 버튼 (첫 번째)
            if (images.size < 5) {
                item {
                    ImageAddButton(onClick = onImageAddClick)
                }
            }

            // 등록된 이미지들
            items(images.size) { index ->
                ImagePreviewCard(
                    imageUrl = images[index],
                    onRemove = { onImageRemove(index) }
                )
            }
        }
    }
}

@Composable
fun ImageAddButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = RegisterColors.ImagePlaceholder,
        border = BorderStroke(2.dp, RegisterColors.Border)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "이미지 추가",
                tint = RegisterColors.Secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "사진 추가",
                fontSize = 12.sp,
                color = RegisterColors.Secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ImagePreviewCard(
    imageUrl: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(120.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Product Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        // 삭제 버튼
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "삭제",
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun CropTypeSection(
    categories: List<CropCategory>,
    selectedCrop: String,
    onCropSelect: (String) -> Unit
) {
    RegisterSection(title = "작물 종류") {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CropChip(
                    category = category.copy(isSelected = category.name == selectedCrop),
                    onClick = { onCropSelect(category.name) }
                )
            }
        }
    }
}

@Composable
fun CropChip(
    category: CropCategory,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (category.isSelected) RegisterColors.Primary else RegisterColors.ImagePlaceholder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${category.emoji} ${category.name}",
                fontSize = 14.sp,
                fontWeight = if (category.isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (category.isSelected) RegisterColors.OnSurface else RegisterColors.Secondary
            )
        }
    }
}

@Composable
fun PriceSection(
    price: String,
    unit: String,
    onPriceChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    RegisterSection(title = "가격") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("가격을 입력하세요") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text(
                text = "원 /",
                fontSize = 16.sp,
                color = RegisterColors.OnSurface
            )

            // 단위 선택
            val units = listOf("kg", "개", "묶음", "박스")
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(100.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    units.forEach { unitOption ->
                        DropdownMenuItem(
                            text = { Text(unitOption) },
                            onClick = {
                                onUnitChange(unitOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySection(
    quantity: String,
    onQuantityChange: (String) -> Unit
) {
    RegisterSection(title = "수량") {
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("판매할 수량을 입력하세요") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

@Composable
fun HarvestDateSection(
    harvestDate: String,
    onDateChange: (String) -> Unit
) {
    RegisterSection(title = "수확 날짜") {
        OutlinedTextField(
            value = harvestDate,
            onValueChange = onDateChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 2024.07.15") },
            singleLine = true
        )
    }
}

@Composable
fun OrganicSection(
    isOrganic: Boolean,
    onOrganicChange: (Boolean) -> Unit
) {
    RegisterSection(title = "재배 방식") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(
                checked = isOrganic,
                onCheckedChange = onOrganicChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RegisterColors.Primary,
                    checkedTrackColor = RegisterColors.Primary.copy(alpha = 0.5f)
                )
            )
            Text(
                text = if (isOrganic) "유기농 재배" else "일반 재배",
                fontSize = 16.sp,
                color = RegisterColors.OnSurface
            )
        }
    }
}

@Composable
fun PickupLocationSection(
    location: String,
    onLocationChange: (String) -> Unit
) {
    RegisterSection(title = "픽업 장소") {
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("픽업 가능한 장소를 입력하세요") },
            singleLine = true
        )
    }
}

@Composable
fun DescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    RegisterSection(title = "상품 설명") {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("상품에 대한 자세한 설명을 입력하세요") },
            maxLines = 5
        )
    }
}

@Composable
fun RegisterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RegisterColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RegisterColors.OnSurface
        )
        content()
    }
}

@Composable
fun RegisterButtonSection(
    onRegisterClick: () -> Unit,
    isValid: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RegisterColors.Surface)
            .padding(16.dp)
    ) {
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RegisterColors.Primary,
                contentColor = RegisterColors.OnSurface,
                disabledContainerColor = RegisterColors.Border
            )
        ) {
            Text(
                text = "농산물 등록하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun isFormValid(productData: ProductRegistration): Boolean {
    return productData.cropType.isNotEmpty() &&
            productData.price.isNotEmpty() &&
            productData.quantity.isNotEmpty() &&
            productData.description.isNotEmpty() &&
            productData.pickupLocation.isNotEmpty()
}

@Preview(showBackground = true)
@Composable
fun WeCanFarmProductRegisterScreenPreview() {
    WeCanFarmProductRegisterScreen()
}