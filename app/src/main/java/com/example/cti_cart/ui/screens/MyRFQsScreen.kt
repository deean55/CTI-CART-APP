package com.example.cti_cart.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun MyRFQsScreen(navController: NavController) {

    var rfqList by remember { mutableStateOf<List<RFQ>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Fetch RFQs
    LaunchedEffect(Unit) {
        FirebaseRepository.getMyRFQs {
            rfqList = it
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        // Header
        Row {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("My RFQs", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            rfqList.isEmpty() -> {
                Text("No RFQs yet")
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rfqList) { rfq ->
                        RFQCard(
                            rfq = rfq,
                            navController = navController, // 🔥 PASS HERE
                            onDelete = {
                                FirebaseRepository.getMyRFQs {
                                    rfqList = it
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------- RFQ CARD --------------------

@Composable
fun RFQCard(
    rfq: RFQ,
    navController: NavController, // 🔥 FIX ADDED
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F0F7)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Part: ${rfq.partName}")
            Text("Qty: ${rfq.quantity}")
            Text("Machine: ${rfq.machine}")
            Text("Required By: ${formatDate(rfq.requiredBy)}")

            Spacer(modifier = Modifier.height(12.dp))

            // 🔥 View Drawing (IN-APP)
            if (rfq.fileUrl.isNotEmpty()) {
                Button(
                    onClick = {
                        val encodedUrl = Uri.encode(rfq.fileUrl)
                        navController.navigate("viewer/$encodedUrl")
                    }
                ) {
                    Text("View Drawing")
                }
            } else {
                Text(
                    text = "No Drawing Attached",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Delete
            TextButton(
                onClick = {
                    FirebaseRepository.firestore
                        .collection("rfqs")
                        .document(rfq.id)
                        .delete()
                        .addOnSuccessListener {
                            onDelete()
                        }
                }
            ) {
                Text("Delete", color = Color.Red)
            }
        }
    }
}

// -------------------- DATE FORMAT --------------------

fun formatDate(date: String): String {
    return try {
        val parts = date.split("/")
        "${parts[0]} ${getMonth(parts[1].toInt())}, ${parts[2]}"
    } catch (e: Exception) {
        date
    }
}

fun getMonth(month: Int): String {
    return listOf(
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    )[month - 1]
}