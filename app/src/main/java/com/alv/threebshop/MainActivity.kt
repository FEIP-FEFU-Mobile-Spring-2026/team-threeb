// app/src/main/java/com/alv/threebshop/MainActivity.kt
package com.alv.threebshop


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alv.threebshop.models.Product
import com.alv.threebshop.ui.details.ProductBottomSheet
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThreeBShopApp()
        }
    }
}

@Composable
fun ThreeBShopApp() {
    val catalogViewModel: CatalogViewModel = viewModel()
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = bottomNavIndex == 0,
                    onClick = { bottomNavIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Каталог") },
                    label = { Text("Каталог") }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = { bottomNavIndex = 1 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Корзина") },
                    label = { Text("Корзина") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (bottomNavIndex == 0) {
                CatalogScreen(viewModel = catalogViewModel)
            } else {
                CartScreen()
            }
        }
    }
}

@Composable
fun CatalogScreen(viewModel: CatalogViewModel) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }


    // Bottom Sheet
    if (selectedProduct != null) {
        ProductBottomSheet(
            product = selectedProduct!!,
            onDismiss = { selectedProduct = null },
            onAddToCart = { /* TODO: логика добавления */ }
        )
    }

    Column {
        if (viewModel.uiState.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Загрузка товаров...")
            }
            return@Column
        }
        viewModel.uiState.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retryLoad() }) {
                        Text("Повторить")
                    }
                }
            }
            return@Column
        }

        // Безопасно находим индекс
        val selectedIndex = viewModel.uiState.categories.indexOf(viewModel.uiState.selectedCategory)
        val safeIndex = if (selectedIndex != -1) selectedIndex else 0

        TabRow(selectedTabIndex = safeIndex) {
            viewModel.uiState.categories.forEach { category ->
                Tab(
                    selected = viewModel.uiState.selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    text = { Text(category) }
                )
            }
        }

        LazyColumn {
            items(
                items = viewModel.getFilteredProducts(),
                key = { product -> product.id }  // ← Добавьте ключ
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { selectedProduct = product }
                )
            }
        }
    }
}

@Composable
fun ProductCard(product: Product,
                onClick: () -> Unit ) {  // ← Убрали models. из пути
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Изображение
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Цена в рублях (копейки → рубли)
                val priceRubles = product.priceInKopecks / 100.0
                Text(
                    text = String.format(Locale.getDefault(), "%.2f ₽", priceRubles),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Категория: ${product.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CartScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Корзина пока пуста", style = MaterialTheme.typography.headlineMedium)
    }
}