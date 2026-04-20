package com.example.cti_cart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.statusBarsPadding

// -------------------- COMMON --------------------

fun logout(navController: NavController) {
    FirebaseRepository.auth.signOut()
    navController.navigate("welcome") {
        popUpTo("dashboard") { inclusive = true }
    }
}

@Composable
fun DashboardHeader(
    title: String,
    navController: NavController,
    showSwitch: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Row {
            if (showSwitch) {
                TextButton(onClick = {
                    navController.navigate("role_selection") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }) {
                    Text("Switch")
                }
            }

            TextButton(onClick = {
                logout(navController)
            }) {
                Text("Logout")
            }
        }
    }
}

// -------------------- SUPPLIER DASHBOARD --------------------

@Composable
fun SupplierDashboardScreen(navController: NavController) {

    var showHistory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        // Header
        DashboardHeader("Supplier Dashboard", navController)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Cards
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            DashboardCard(
                title = "Add Details",
                icon = Icons.Default.Edit,
                color = Color(0xFF1976D2)
            ) {
                navController.navigate("add_details")
            }

            DashboardCard(
                title = "Add Machine",
                icon = Icons.Default.Build,
                color = Color(0xFF388E3C)
            ) {
                navController.navigate("add_machine")
            }

            DashboardCard(
                title = if (showHistory) "History ▲" else "History ▼",
                icon = Icons.Default.History,
                color = Color(0xFFF57C00)
            ) {
                showHistory = !showHistory
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- INLINE MACHINE LIST --------

        if (showHistory) {

            Text(
                text = "Machine History",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            MachineListSection()
        }
    }
}

// -------------------- DASHBOARD CARD --------------------

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// -------------------- BUYER DASHBOARD --------------------

@Composable
fun BuyerDashboardScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        DashboardHeader("Buyer Dashboard", navController)

        Spacer(modifier = Modifier.height(16.dp))

        TwoCardRow(
            "Post Requirement",
            Color(0xFF1976D2),
            { navController.navigate("post_rfq") },

            "My RFQs",
            Color(0xFF388E3C),
            { navController.navigate("my_rfqs") }
        )

        TwoCardRow(
            "Orders",
            Color(0xFFF57C00),
            { navController.navigate("buyer_orders") },

            "Saved Suppliers",
            Color(0xFF7B1FA2),
            { navController.navigate("saved_suppliers") }
        )

        TwoCardRow(
            "Browse Suppliers",
            Color(0xFFD32F2F),
            { navController.navigate("browse_suppliers") },

            "Messages",
            Color(0xFF455A64),
            { navController.navigate("chat_list") }
        )
    }
}

// -------------------- REUSABLE UI --------------------

@Composable
fun TwoCardRow(
    title1: String,
    color1: Color,
    onClick1: () -> Unit,
    title2: String,
    color2: Color,
    onClick2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ActionCard(
            title = title1,
            color = color1,
            modifier = Modifier.weight(1f),
            onClick = onClick1
        )

        if (title2.isNotEmpty()) {
            ActionCard(
                title = title2,
                color = color2,
                modifier = Modifier.weight(1f),
                onClick = onClick2
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun ActionCard(
    title: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(enabled = title.isNotEmpty()) { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}