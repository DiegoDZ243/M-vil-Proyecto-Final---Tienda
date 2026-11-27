package com.example.proyectofinal.ui.detalle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.proyectofinal.ui.TiendaViewModel

@Composable
fun DetailPane(
    productoId: Int?,
    viewModel: TiendaViewModel,
    modifier: Modifier = Modifier
) {
    if (productoId == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona un producto para ver los detalles",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        ProductDetailScreen(
            productId = productoId,
            viewModel = viewModel,
            onNavigateBack = {},
            isEmbedded = true,
            modifier = modifier
        )
    }
}
