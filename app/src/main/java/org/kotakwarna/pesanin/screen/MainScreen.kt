package org.kotakwarna.pesanin.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.kotakwarna.pesanin.viewmodels.CreateOrderViewModel
import org.kotakwarna.pesanin.viewmodels.MainViewModel
import org.kotakwarna.pesanin.viewmodels.UiEvent

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Scoped to the nav graph so CreateOrderScreen shares this instance
    val navBackStackEntry = remember(currentBackStack) {
        navController.getBackStackEntryOrNull(Screen.CreateOrder.route)
    }
    val createOrderViewModel: CreateOrderViewModel? = navBackStackEntry?.let {
        hiltViewModel(it)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                UiEvent.Idle -> {}
                is UiEvent.Navigate -> navController.navigate(it.screen.route)
                UiEvent.NavigateBack -> navController.navigateUp()
                is UiEvent.Snackbar -> snackbarHostState.showSnackbar(it.msg)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PesaninTopBar(navController, viewModel, createOrderViewModel)
        },
        bottomBar = { PesaninNavBar(navController) },
        floatingActionButton = {
            PesaninFAB(currentRoute, createOrderViewModel)
        }
    ) { innerPadding ->
        PesaninNavHost(
            navController,
            modifier = Modifier.padding(innerPadding),
            viewModel,
        )
    }
}

@Composable
fun PesaninFAB(
    currentRoute: String?,
    createOrderViewModel: CreateOrderViewModel?
) {
    val createOrderState by createOrderViewModel?.uiState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }

    if (currentRoute == Screen.CreateOrder.route) {
        CartFab(
            itemCount = createOrderState?.totalQty ?: 0,
            total = createOrderState?.totalAmount ?: 0.0,
            onClick = { createOrderViewModel?.showSummary() }
        )
    }
}
