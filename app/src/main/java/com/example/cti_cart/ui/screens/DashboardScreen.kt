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

// 🔥 UPDATED HEADER WITH USERNAME
@Composable
fun DashboardHeader(
    title: String,
    navController: NavController,
    showSwitch: Boolean = true,
    showUserName: Boolean = false
) {
    var userName by remember { mutableStateOf("Loading...") }

    // 🔥 Fetch username
    LaunchedEffect(Unit) {
        val userId = FirebaseRepository.auth.currentUser?.uid

        if (userId != null) {
            FirebaseRepository.firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener {
                    userName = it.getString("fullName") ?: "User"
                }
                .addOnFailureListener {
                    userName = "User"
                }
        }
    }

    Column {

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

        // 🔥 Username below title
        if (showUserName) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hi, $userName 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
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

        DashboardHeader(
            title = "Supplier Dashboard",
            navController = navController,
            showUserName = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            DashboardCard(
                "Add Details",
                Icons.Default.Edit,
                Color(0xFF1976D2)
            ) { navController.navigate("add_details") }

            DashboardCard(
                "Add Machine",
                Icons.Default.Build,
                Color(0xFF388E3C)
            ) { navController.navigate("add_machine") }

            DashboardCard(
                if (showHistory) "History ▲" else "History ▼",
                Icons.Default.History,
                Color(0xFFF57C00)
            ) { showHistory = !showHistory }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showHistory) {
            Text("Machine History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            MachineListSection(navController)
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

        // 🔥 UPDATED HEADER CALL
        DashboardHeader(
            title = "Buyer Dashboard",
            navController = navController,
            showUserName = true // 👈 IMPORTANT
        )

        Spacer(modifier = Modifier.height(16.dp))

        TwoCardRow(
            "POST NEW JOB",
            Color(0xFF1976D2),
            { navController.navigate("post_job") },

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

        ActionCard(title1, color1, Modifier.weight(1f), onClick1)

        if (title2.isNotEmpty()) {
            ActionCard(title2, color2, Modifier.weight(1f), onClick2)
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