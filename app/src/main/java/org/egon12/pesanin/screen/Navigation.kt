package org.egon12.pesanin.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun PesaninNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CreateOrder.route,
    ) {
        composable(Screen.CreateOrder.route) {
            CreateOrderScreen(modifier)
        }

        composable(Screen.Product.route) {
            ProductListScreen({}, {})
        }
    }
}

@Composable
fun PesaninNavBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        Screen.bottomNavScreens.forEach {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = it.outlinedIcon,
                        contentDescription = it.title,
                    )
                },
                label = { Text(it.title) },
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
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    object CreateOrder :
        Screen(
            route = "createOrder",
            title = "Pes. Baru",
            filledIcon = Icons.Default.AddShoppingCart,
            outlinedIcon = Icons.Outlined.AddShoppingCart
        )

    object Product :
        Screen(
            route = "product",
            title = "Daftar Harga",
            filledIcon = Icons.Default.PriceCheck,
            outlinedIcon = Icons.Outlined.PriceCheck
        )

    object Orders :
        Screen(
            route = "orders",
            title = "Pesanan",
            filledIcon = Icons.Default.Checklist,
            outlinedIcon = Icons.Outlined.Checklist,
        )

    object Settings :
        Screen(
            route = "settings",
            title = "Pengaturan",
            filledIcon = Icons.Default.Settings,
            outlinedIcon = Icons.Outlined.Settings
        )

    companion object {
        // Bottom nav items
        val bottomNavScreens: List<Screen> = listOf(
            CreateOrder,
            Product,
            Orders,
            Settings,
        )
    }
}