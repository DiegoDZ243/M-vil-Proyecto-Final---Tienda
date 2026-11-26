package com.example.proyectofinal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.R

private val ColorEstrella = Color(0xFFFFB300)

/** RF-19: muestra la calificación en escala de 1 a 5 con estrellas (solo lectura). */
@Composable
fun RatingBar(
    calificacion: Double,
    numReviews: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    mostrarTexto: Boolean = true
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (numReviews <= 0) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(starSize)
            )
            if (mostrarTexto) {
                Text(
                    text = stringResource(R.string.sin_review),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Row
        }
        val valor = calificacion.coerceIn(0.0, 5.0)
        for (i in 1..5) {
            val icono = when {
                valor >= i - 0.25 -> Icons.Default.Star
                valor >= i - 0.75 -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = ColorEstrella,
                modifier = Modifier.size(starSize)
            )
        }
        if (mostrarTexto) {
            Text(
                text = " %.1f/5 (%d)".format(valor, numReviews),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** RF-20: selector interactivo de 1 a 5 estrellas. */
@Composable
fun RatingSelector(
    valor: Int,
    onValorChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= valor) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "$i estrellas",
                tint = ColorEstrella,
                modifier = Modifier
                    .size(starSize)
                    .clickable { onValorChange(i) }
            )
        }
    }
}
