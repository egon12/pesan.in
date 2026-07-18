package org.kotakwarna.pesanin.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import org.kotakwarna.pesanin.R
import org.kotakwarna.pesanin.model.Order
import org.kotakwarna.pesanin.model.OrderStatus
import org.kotakwarna.pesanin.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()

    if (orders.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.msg_no_orders), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    onStatusChange = { viewModel.updateStatus(order, it) },
                    onOrderClick = { navController.navigate("orderDetail/${order.id}") }
                )
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onStatusChange: (OrderStatus) -> Unit, onOrderClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onOrderClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.customerName.ifBlank { stringResource(R.string.placeholder_no_name) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text(
                            text = order.status.label(),
                            style = MaterialTheme.typography.labelMedium,
                            color = order.status.color(),
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.action_change_status),
                            tint = order.status.color(),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        OrderStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status.label(),
                                        color = status.color(),
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.label_wa, order.whatsappNumber),
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(order.orderDate)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatter.format(order.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun OrderStatus.label(): String = when (this) {
    OrderStatus.PENDING -> stringResource(R.string.status_pending)
    OrderStatus.COMPLETED -> stringResource(R.string.status_completed)
    OrderStatus.CANCELED -> stringResource(R.string.status_cancelled)
}

@Composable
private fun OrderStatus.color(): Color = when (this) {
    OrderStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    OrderStatus.COMPLETED -> Color(0xFF2E7D32)
    OrderStatus.CANCELED -> MaterialTheme.colorScheme.error
}
