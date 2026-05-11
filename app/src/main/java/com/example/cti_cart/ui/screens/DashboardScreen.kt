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
import androidx.compose.ui.text.style.TextAlign
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

        // FIRST ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DashboardCard(
                title = "Add Details",
                icon = Icons.Default.Edit,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("add_details")
            }

            DashboardCard(
                title = "Add/View Machines",
                icon = Icons.Default.Build,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("add_machine")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECOND ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            DashboardCard(
                title = "Buyer RFQs",
                icon = Icons.Default.List,
                color = Color(0xFF8E24AA),
                modifier = Modifier.weight(1f)
            ) {
                navController.navigate("my_rfqs")
            }

            DashboardCard(
                title = if (showHistory) "History ▲" else "History ▼",
                icon = Icons.Default.History,
                color = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            ) {
                showHistory = !showHistory
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showHistory) {
            Text(
                "Machine History",
                style = MaterialTheme.typography.titleMedium
            )

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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
            .height(120.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
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