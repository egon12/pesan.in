package org.egon12.pesanin.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.egon12.pesanin.R
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import org.egon12.pesanin.viewmodels.CreateOrderViewModel
import org.egon12.pesanin.viewmodels.MainViewModel

fun NavHostController.getBackStackEntryOrNull(route: String): NavBackStackEntry? {
    return try {
        getBackStackEntry(route)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun PesaninNavHost(
    navController: NavHostController,
    modifier: Modifier,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CreateOrder.route,
    ) {
        composable(Screen.CreateOrder.route) {
            CreateOrderScreen(modifier)
        }

        composable(Screen.Product.route) {
            ProductListScreen(modifier, {})
        }

        composable(Screen.Orders.route) {
            OrdersScreen(modifier, navController)
        }

        composable("orderDetail/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(modifier, navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(modifier)
        }

        composable(Screen.CreateProduct.route) {
            ProductFormScreen(
                modifier,
                onError = { viewModel.alert(it) },
                productId = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesaninTopBar(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    createOrderViewMode: CreateOrderViewModel?,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    when (currentRoute) {
        Screen.Product.route -> {
            ProductListTopBar(
                onNavigateToCreateProduct = { mainViewModel.navigate(Screen.CreateProduct) },
                onNavigateToImportProduct = {},
            )
        }

        Screen.CreateProduct.route -> {
            ProductFormTopBar(productId = null)
        }

        Screen.CreateOrder.route -> {
            TopAppBar(
                title = { Text(stringResource(R.string.title_new_order)) },
                actions = {
                    IconButton(onClick = { createOrderViewMode?.clearCart() }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_all))
                    }
                }
            )
        }

        Screen.Orders.route -> {
            TopAppBar(
                title = { Text(stringResource(R.string.title_orders_list)) }
            )
        }

        Screen.Settings.route -> {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) }
            )
        }


        // Add more routes as needed
    }
}

@Composable
fun PesaninNavBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavScreens: List<Screen> = remember {
        listOf(
            Screen.CreateOrder,
            Screen.Product,
            Screen.Orders,
            Screen.Settings,
        )
    }

    NavigationBar {
        bottomNavScreens.forEach {
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = it.outlinedIcon),
                        contentDescription = it.title(),
                        modifier = Modifier.size(36.dp)
                    )
                },
                label = { Text(it.title()) },
                selected = currentDestination?.route?.startsWith(it.route) == true,
                onClick = {
                    navController.navigate(it.route) {
                        // Pop up to the start destination to avoid back stack buildup
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }

}


sealed class Screen(
    val route: String,
    val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: Int,
) {
    @Composable
    fun title() = stringResource(titleRes)

    object CreateOrder :
        Screen(
            route = "createOrder",
            titleRes = R.string.nav_create_order,
            filledIcon = Icons.Default.AddShoppingCart,
            outlinedIcon = R.drawable.create_order_icon,
        )

    object Product :
        Screen(
            route = "products",
            titleRes = R.string.nav_products,
            filledIcon = Icons.Default.PriceCheck,
            outlinedIcon = R.drawable.price_list_icon
        )

    object Orders :
        Screen(
            route = "orders",
            titleRes = R.string.nav_orders,
            filledIcon = Icons.Default.Checklist,
            outlinedIcon = R.drawable.orders_icon
        )

    object Settings :
        Screen(
            route = "settings",
            titleRes = R.string.nav_settings,
            filledIcon = Icons.Default.Settings,
            outlinedIcon = R.drawable.settings_icon
        )

    companion object {
        // Bottom nav items
        val bottomNavScreens: List<Screen> = listOf(
            CreateOrder,
            Product,
            Orders,
            Settings,
        ).toList()
    }

    object CreateProduct :
        Screen(
            route = "product/create",
            titleRes = R.string.title_add_product,
            filledIcon = Icons.Default.PriceCheck,
            outlinedIcon = R.drawable.price_list_icon,
        )

    object ImportCSVProduct :
        Screen(
            route = "product/import",
            titleRes = R.string.action_import_csv,
            filledIcon = Icons.Default.PriceCheck,
            outlinedIcon = R.drawable.price_list_icon,
        )
}
