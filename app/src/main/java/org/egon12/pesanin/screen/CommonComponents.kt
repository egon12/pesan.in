package org.egon12.pesanin.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

val formatter = DecimalFormat("Rp#,###.##")

@Composable
fun CartFab(
    itemCount: Int,
    total: Double,
    onClick: () -> Unit,
) {
    if (itemCount == 0) return

    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("$itemCount item  •  ${formatter.format(total)}")
    }
}