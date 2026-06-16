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
import com.google.firebase.firestore.FieldValue



private const val RFQ_RADIUS_KM = 10.0

data class RFQWithDistance(
    val rfq: RFQ,
    val distanceKm: Double
)
@Composable
fun BuyerRFQsScreen(navController: NavController) {

    var rfqList by remember {
        mutableStateOf<List<RFQWithDistance>>(emptyList())
    }
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
                        mutableListOf<RFQWithDistance>()

                    if (allRfqs.isEmpty()) {
                        rfqList = emptyList()
                        isLoading = false
                        return@getAllRFQs
                    }

                    allRfqs.forEach { rfq ->

                        val results = FloatArray(1)

                        android.location.Location.distanceBetween(
                            supplierLat,
                            supplierLng,
                            rfq.latitude,
                            rfq.longitude,
                            results
                        )

                        val distanceKm =
                            results[0] / 1000.0

                        Log.d(
                            "RFQ_DISTANCE",
                            "RFQ=${rfq.partName} Distance=$distanceKm km"
                        )

                        if (distanceKm <= RFQ_RADIUS_KM) {

                            nearbyRfqs.add(
                                RFQWithDistance(
                                    rfq = rfq,
                                    distanceKm = distanceKm
                                )
                            )

                            Log.d(
                                "RFQ_DISTANCE",
                                "Showing RFQ ${rfq.partName}"
                            )
                        }
                    }

                    Log.d(
                        "RFQ_DISTANCE",
                        "Nearby RFQs Count = ${nearbyRfqs.size}"
                    )

                    rfqList = nearbyRfqs
                    isLoading = false
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

                        items(rfqList) { item ->

                            val rfq = item.rfq

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

                                    Text(
                                        text = String.format(
                                            "Distance: %.1f km",
                                            item.distanceKm
                                        ),
                                        color = Color.DarkGray
                                    )

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
                                        val currentSupplierId =
                                            FirebaseRepository.auth.currentUser?.uid ?: ""

                                        val isInterested =
                                            rfq.interestedSuppliers.contains(currentSupplierId)

                                        OutlinedButton(
                                            modifier = Modifier.weight(1f),
                                            enabled = !isInterested,
                                            onClick = {

                                                val supplierId =
                                                    FirebaseRepository.auth.currentUser?.uid ?: ""

                                                val buyerId = rfq.userId

                                                val chatId = "${rfq.id}_${supplierId}"

                                                Log.d("CHAT_DEBUG", "RFQ ID = ${rfq.id}")
                                                Log.d("CHAT_DEBUG", "Buyer ID = $buyerId")
                                                Log.d("CHAT_DEBUG", "Supplier ID = $supplierId")
                                                Log.d("CHAT_DEBUG", "Chat ID = $chatId")

                                                FirebaseRepository.firestore
                                                    .collection("rfqs")
                                                    .document(rfq.id)
                                                    .update(
                                                        "interestedSuppliers",
                                                        FieldValue.arrayUnion(currentSupplierId)
                                                    )
                                                    .addOnSuccessListener {

                                                        Log.d("CHAT_DEBUG", "Interested saved")

                                                        FirebaseRepository.firestore
                                                            .collection("chats")
                                                            .document(chatId)
                                                            .set(
                                                                hashMapOf(
                                                                    "rfqId" to rfq.id,
                                                                    "buyerId" to buyerId,
                                                                    "supplierId" to supplierId,
                                                                    "createdAt" to System.currentTimeMillis()
                                                                )
                                                            )
                                                            .addOnSuccessListener {

                                                                Log.d("CHAT_DEBUG", "Chat created successfully")

                                                                navController.navigate("buyer_rfqs") {
                                                                    popUpTo("buyer_rfqs") {
                                                                        inclusive = true
                                                                    }
                                                                }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e(
                                                                    "CHAT_DEBUG",
                                                                    "Chat create failed",
                                                                    e
                                                                )
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(
                                                            "CHAT_DEBUG",
                                                            "Interested save failed",
                                                            e
                                                        )
                                                    }
                                            }
                                        ) {
                                            Text(
                                                if (isInterested)
                                                    "Interested ✓"
                                                else
                                                    "Interested"
                                            )
                                        }

                                        // CHAT BUYER
                                        Button(
                                            enabled = isInterested,
                                            onClick = {
                                                val supplierId =
                                                    FirebaseRepository.auth.currentUser?.uid ?: ""

                                                val chatId = "${rfq.id}_${supplierId}"

                                                navController.navigate(
                                                    "chat/$chatId"
                                                )
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