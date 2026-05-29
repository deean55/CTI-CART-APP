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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ

@Composable
fun BuyerRFQsScreen(navController: NavController) {

    var rfqList by remember { mutableStateOf<List<RFQ>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        FirebaseRepository.getAllRFQs {
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

                                                    FirebaseRepository.getAllRFQs {
                                                        rfqList = it
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