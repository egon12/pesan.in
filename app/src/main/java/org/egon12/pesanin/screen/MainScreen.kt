package org.egon12.pesanin.screen

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import org.egon12.pesanin.viewmodels.MainViewModel
import org.egon12.pesanin.viewmodels.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    LaunchedEffect("MainScreen") {

        viewModel.events.collect {
            when (it) {
                UiEvent.Idle -> {}
                is UiEvent.Navigate -> navController.navigate(it.screen.route)
                UiEvent.NavigateBack -> navController.popBackStack()
                is UiEvent.Snackbar -> snackbarHostState.showSnackbar(it.msg)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PesaninTopBar(
                navController,
                onAddProduct = { navController.navigate(Screen.CreateProduct.route) }
            )
        },
        bottomBar = { PesaninNavBar(navController) }
    ) { innerPadding ->
        PesaninNavHost(
            navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}


