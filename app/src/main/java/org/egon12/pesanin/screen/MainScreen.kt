package org.egon12.pesanin.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.egon12.pesanin.viewmodels.CartFab
import org.egon12.pesanin.viewmodels.CreateOrderSideEffect
import org.egon12.pesanin.viewmodels.CreateOrderViewModel
import org.egon12.pesanin.viewmodels.MainViewModel
import org.egon12.pesanin.viewmodels.UiEvent

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Scoped to the nav graph so CreateOrderScreen shares this instance
    val navBackStackEntry = remember(currentBackStack) {
        try {
            navController.getBackStackEntry(Screen.CreateOrder.route)
        } catch (_: Exception) {
            null
        }
    }
    val createOrderViewModel: CreateOrderViewModel? = navBackStackEntry?.let {
        hiltViewModel(it)
    }
    val createOrderState by createOrderViewModel?.uiState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(null) }

    LaunchedEffect(createOrderViewModel) {
        createOrderViewModel?.sideEffect?.collect { effect ->
            when (effect) {
                is CreateOrderSideEffect.OpenWhatsApp -> {
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=${effect.phoneNumber}&text=${Uri.encode(effect.message)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            }
        }
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
            if (currentRoute == Screen.CreateOrder.route) {
                CartFab(
                    itemCount = createOrderState?.items?.sumOf { it.qty } ?: 0,
                    total = createOrderState?.totalAmount ?: 0.0,
                    onClick = { createOrderViewModel?.showSummary() }
                )
            }
        }
    ) { innerPadding ->
        PesaninNavHost(
            navController,
            modifier = Modifier.padding(innerPadding),
            viewModel,
        )
    }
}
