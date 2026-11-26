package com.example.proyectofinal.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.example.proyectofinal.R

data class IdiomaOption(
    val code: String,
    val labelRes: Int
)

val listaIdiomas = listOf(
    IdiomaOption("es", R.string.idioma_es),
    IdiomaOption("en", R.string.idioma_en),
    IdiomaOption("fr", R.string.idioma_fr),
    IdiomaOption("pt", R.string.idioma_pt)
)

@Composable
fun LanguageSelectorDialog(
    onDismissRequest: () -> Unit
) {
    val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "es" }
    var selectedCode by remember { mutableStateOf(currentTag.take(2)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.seleccionar_idioma)) },
        text = {
            Column {
                listaIdiomas.forEach { idioma ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCode = idioma.code }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedCode == idioma.code),
                            onClick = { selectedCode = idioma.code }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(idioma.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val appLocale = LocaleListCompat.forLanguageTags(selectedCode)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancelar))
            }
        }
    )
}
