package com.example.cti_cart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewQuoteScreen(
    navController: NavController,
    rfqId: String
) {
    var rfq by remember { mutableStateOf<RFQ?>(null) }
    var supplierName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var deliveryTime by remember { mutableStateOf("") }
    var validity by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    LaunchedEffect(rfqId) {
        val supplierId = FirebaseRepository.auth.currentUser?.uid

        if (supplierId == null) {
            errorMessage = "Please log in again."
            isLoading = false
            return@LaunchedEffect
        }

        FirebaseRepository.firestore
            .collection("rfqs")
            .document(rfqId)
            .get()
            .addOnSuccessListener { document ->
                val loadedRfq = document.toObject(RFQ::class.java)?.copy(id = document.id)

                if (loadedRfq == null) {
                    errorMessage = "RFQ not found."
                } else {
                    rfq = loadedRfq
                    quantity = loadedRfq.quantity
                }

                FirebaseRepository.firestore
                    .collection("users")
                    .document(supplierId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        supplierName = userDoc.getString("name")
                            ?: userDoc.getString("companyName")
                                    ?: ""
                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                errorMessage = it.message ?: "Unable to load RFQ."
                isLoading = false
            }
    }

    val totalAmount = (quantity.toDoubleOrNull() ?: 0.0) *
            (unitPrice.toDoubleOrNull() ?: 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Quote") }
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            rfq?.let { currentRfq ->
                Text(
                    text = "Quote for RFQ",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Part: ${currentRfq.partName}")
                Text("Machine: ${currentRfq.machine}")
                Text("Buyer RFQ Quantity: ${currentRfq.quantity}")
                Text("Required By: ${formatDate(currentRfq.requiredBy)}")

                HorizontalDivider()

                if (supplierName.isNotEmpty()) {
                    Text("Supplier: $supplierName")
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text("Unit Price (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "Total Amount: ₹ %.2f",
                        totalAmount
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = deliveryTime,
                    onValueChange = { deliveryTime = it },
                    label = { Text("Delivery Time") },
                    placeholder = { Text("e.g. 15 days") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = validity,
                    onValueChange = { validity = it },
                    label = { Text("Quote Validity") },
                    placeholder = { Text("e.g. 30 days") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = paymentTerms,
                    onValueChange = { paymentTerms = it },
                    label = { Text("Payment Terms") },
                    placeholder = { Text("e.g. 50% advance, balance before dispatch") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving &&
                                quantity.toDoubleOrNull() != null &&
                                (quantity.toDoubleOrNull() ?: 0.0) > 0 &&
                                unitPrice.toDoubleOrNull() != null &&
                                (unitPrice.toDoubleOrNull() ?: 0.0) >= 0,
                        onClick = {
                            val supplierId =
                                FirebaseRepository.auth.currentUser?.uid ?: return@Button

                            isSaving = true
                            errorMessage = ""

                            val quoteData = hashMapOf<String, Any>(
                                "rfqId" to currentRfq.id,
                                "buyerId" to currentRfq.userId,
                                "supplierId" to supplierId,
                                "supplierName" to supplierName,
                                "partName" to currentRfq.partName,
                                "quantity" to (quantity.toDoubleOrNull() ?: 0.0),
                                "unitPrice" to (unitPrice.toDoubleOrNull() ?: 0.0),
                                "totalAmount" to totalAmount,
                                "deliveryTime" to deliveryTime.trim(),
                                "validity" to validity.trim(),
                                "paymentTerms" to paymentTerms.trim(),
                                "remarks" to remarks.trim(),
                                "status" to "Pending",
                                "createdAt" to System.currentTimeMillis()
                            )

                            FirebaseRepository.saveQuote(
                                quoteData = quoteData,
                                onSuccess = {
                                    isSaving = false
                                    navController.popBackStack()
                                },
                                onFailure = {
                                    isSaving = false
                                    errorMessage =
                                        it.message ?: "Unable to send quote."
                                }
                            )
                        }
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator()
                        } else {
                            Text("Send Quote")
                        }
                    }
                }
            }
        }
    }
}
