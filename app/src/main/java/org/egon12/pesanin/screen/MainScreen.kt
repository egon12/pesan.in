package org.egon12.pesanin.screen

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import org.egon12.pesanin.viewmodels.CreateOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { PesaninNavBar(navController) }
    ) { innerPadding ->
        PesaninNavHost(
            navController,
            modifier = Modifier.padding(innerPadding),
            snackbarHostState = snackbarHostState,
        )
    }
}


