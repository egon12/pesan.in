package org.kotakwarna.pesanin.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.kotakwarna.pesanin.R
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListTopBar(
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToImportProduct: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(stringResource(R.string.title_price_list)) },
        actions = {
            IconButton(onClick = onNavigateToCreateProduct) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_add_product))
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_import_csv)) },
                        onClick = {
                            showMenu = false
                            onNavigateToImportProduct()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Upload, contentDescription = null)
                        }
                    )
                }
            }
        }
    )
}