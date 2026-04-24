package com.example.cti_cart.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ
import com.google.firebase.firestore.Query
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun MyRFQsScreen(navController: NavController) {

    var rfqList by remember { mutableStateOf<List<RFQ>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Fetch data
    LaunchedEffect(Unit) {
        FirebaseRepository.getMyRFQs {
            rfqList = it
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // 🔥 Best practice
            .padding(16.dp) // 🔥 Added top space
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
                LazyColumn {
                    items(rfqList) { rfq ->
                        RFQCard(rfq)
                    }
                }
            }
        }
    }
}

@Composable
fun RFQCard(rfq: RFQ) {

    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Part: ${rfq.partName}")
            Text("Qty: ${rfq.quantity}")
            Text("Machine: ${rfq.machine}")
            Text("Required By: ${rfq.requiredBy}")

            Spacer(modifier = Modifier.height(8.dp))

            if (rfq.fileUrl.isNotEmpty()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(Uri.parse(rfq.fileUrl), "*/*")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                ) {
                    Text("View Drawing")
                }
            }
        }
    }
}