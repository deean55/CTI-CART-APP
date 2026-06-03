package com.example.cti_cart.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ

private const val RFQ_RADIUS_KM = 10.0
@Composable
fun BuyerRFQsScreen(navController: NavController) {

    var rfqList by remember { mutableStateOf<List<RFQ>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        val supplierId =
            FirebaseRepository.auth.currentUser?.uid

        if (supplierId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        FirebaseRepository.firestore
            .collection("users")
            .document(supplierId)
            .get()
            .addOnSuccessListener { supplierDoc ->

                val supplierLat =
                    supplierDoc.getDouble("latitude") ?: 0.0

                val supplierLng =
                    supplierDoc.getDouble("longitude") ?: 0.0

                FirebaseRepository.getAllRFQs { allRfqs ->

                    val nearbyRfqs =
                        mutableListOf<RFQ>()

                    var processedCount = 0

                    if (allRfqs.isEmpty()) {
                        rfqList = emptyList()
                        isLoading = false
                        return@getAllRFQs
                    }

                    allRfqs.forEach { rfq ->

                        FirebaseRepository.firestore
                            .collection("users")
                            .document(rfq.userId)
                            .get()
                            .addOnSuccessListener { buyerDoc ->

                                val buyerLat =
                                    buyerDoc.getDouble("latitude") ?: 0.0

                                val buyerLng =
                                    buyerDoc.getDouble("longitude") ?: 0.0

                                val results = FloatArray(1)

                                android.location.Location.distanceBetween(
                                    supplierLat,
                                    supplierLng,
                                    buyerLat,
                                    buyerLng,
                                    results
                                )

                                val distanceKm =
                                    results[0] / 1000.0

                                //log distance
                                Log.d(
                                    "RFQ_DISTANCE",
                                    "Buyer=${rfq.userId} Distance=$distanceKm km"
                                )
                                if (distanceKm <= RFQ_RADIUS_KM) {
                                    nearbyRfqs.add(rfq)
                                    Log.d(
                                        "RFQ_DISTANCE",
                                        "Showing RFQ ${rfq.partName} from buyer ${rfq.userId}"
                                    )
                                }

                                processedCount++

                                if (processedCount == allRfqs.size) {
                                    Log.d(
                                        "RFQ_DISTANCE",
                                        "Nearby RFQs Count = ${nearbyRfqs.size}"
                                    )
                                    rfqList = nearbyRfqs
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener {

                                processedCount++

                                if (processedCount == allRfqs.size) {
                                    Log.d(
                                        "RFQ_DISTANCE",
                                        "Nearby RFQs Count = ${nearbyRfqs.size}"
                                    )
                                    rfqList = nearbyRfqs
                                    isLoading = false
                                }
                            }
                    }
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {

            Row {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                Text(
                    "Buyer RFQs",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {

                isLoading -> {
                    CircularProgressIndicator()
                }

                rfqList.isEmpty() -> {
                    Text("No RFQs available")
                }

                else -> {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {

                        items(rfqList) { rfq ->

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF3F0F7)
                                ),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    // TITLE + STATUS
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Text(
                                            text = rfq.partName,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        RFQStatusBadge(rfq.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("Qty: ${rfq.quantity}")
                                    Text("Machine: ${rfq.machine}")
                                    Text("Required By: ${formatDate(rfq.requiredBy)}")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // FIRST ROW
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        // VIEW DRAWING
                                        if (rfq.fileUrl.isNotEmpty()) {

                                            OutlinedButton(
                                                modifier = Modifier.weight(1f),
                                                onClick = {

                                                    val encodedUrl =
                                                        Uri.encode(rfq.fileUrl)

                                                    navController.navigate(
                                                        "viewer/$encodedUrl"
                                                    )
                                                }
                                            ) {
                                                Text("Drawing")
                                            }
                                        }

                                        // SEND QUOTE
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            onClick = {

                                                println("Send Quote Clicked")
                                            }
                                        ) {
                                            Text("Quote")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // SECOND ROW
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        // INTERESTED
                                        OutlinedButton(
                                            modifier = Modifier.weight(1f),
                                            onClick = {

                                                FirebaseRepository.firestore
                                                    .collection("rfqs")
                                                    .document(rfq.id)
                                                    .update(
                                                        "status",
                                                        "Quoted"
                                                    )
                                                    .addOnSuccessListener {

                                                        // refresh screen
                                                        navController.navigate("buyer_rfqs") {
                                                            popUpTo("buyer_rfqs") { inclusive = true }
                                                        }
                                                    }
                                            }
                                        ) {
                                            Text("Interested")
                                        }

                                        // CHAT BUYER
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2E7D32)
                                            ),
                                            onClick = {

                                                println("Chat Buyer Clicked")
                                            }
                                        ) {
                                            Text("Chat")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}