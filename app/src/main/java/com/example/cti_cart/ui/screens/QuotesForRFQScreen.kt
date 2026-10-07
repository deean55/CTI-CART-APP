package com.example.cti_cart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.Quote
import java.util.Locale

@Composable
fun QuotesForRFQScreen(
    navController: NavController,
    rfqId: String
) {
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    fun loadQuotes() {
        isLoading = true
        errorMessage = ""
        FirebaseRepository.getQuotesForRFQ(
            rfqId = rfqId,
            onResult = {
                quotes = it
                isLoading = false
            },
            onFailure = {
                errorMessage = it.message ?: "Unable to load quotes."
                isLoading = false
            }
        )
    }

    LaunchedEffect(rfqId) {
        loadQuotes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text("Quotes", style = MaterialTheme.typography.titleLarge)
                Text(
                    "RFQ: $rfqId",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> CircularProgressIndicator()
            errorMessage.isNotEmpty() -> Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error
            )
            quotes.isEmpty() -> Text(
                "No quotes received yet.",
                color = Color.Gray
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        onStatusChanged = { loadQuotes() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    onStatusChanged: () -> Unit
) {
    var isUpdating by remember(quote.id, quote.status) { mutableStateOf(false) }
    var actionError by remember(quote.id) { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quote.supplierName.ifBlank { "Supplier" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Part: ${quote.partName}")
                }
                Text(
                    quote.status,
                    color = when (quote.status) {
                        "Accepted" -> Color(0xFF2E7D32)
                        "Rejected" -> Color(0xFFC62828)
                        "Negotiation" -> Color(0xFFF57C00)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Quantity: ${formatNumber(quote.quantity)}")
            Text("Unit Price: ₹${formatMoney(quote.unitPrice)}")
            Text(
                "Total Amount: ₹${formatMoney(quote.totalAmount)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Delivery: ${quote.deliveryTime}")
            Text("Validity: ${quote.validity}")
            Text("Payment: ${quote.paymentTerms}")

            if (quote.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Remarks: ${quote.remarks}")
            }

            if (actionError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(actionError, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isUpdating) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (quote.status == "Pending" || quote.status == "Negotiation") {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isUpdating = true
                                actionError = ""
                                FirebaseRepository.updateQuoteStatus(
                                    quoteId = quote.id,
                                    status = "Accepted",
                                    onSuccess = {
                                        isUpdating = false
                                        onStatusChanged()
                                    },
                                    onFailure = {
                                        isUpdating = false
                                        actionError = it.message ?: "Unable to accept quote."
                                    }
                                )
                            }
                        ) {
                            Text("Accept")
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isUpdating = true
                                actionError = ""
                                FirebaseRepository.updateQuoteStatus(
                                    quoteId = quote.id,
                                    status = "Rejected",
                                    onSuccess = {
                                        isUpdating = false
                                        onStatusChanged()
                                    },
                                    onFailure = {
                                        isUpdating = false
                                        actionError = it.message ?: "Unable to reject quote."
                                    }
                                )
                            }
                        ) {
                            Text("Reject")
                        }
                    }
                }

                if (quote.status == "Pending") {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            isUpdating = true
                            actionError = ""
                            FirebaseRepository.updateQuoteStatus(
                                quoteId = quote.id,
                                status = "Negotiation",
                                onSuccess = {
                                    isUpdating = false
                                    onStatusChanged()
                                },
                                onFailure = {
                                    isUpdating = false
                                    actionError = it.message ?: "Unable to start negotiation."
                                }
                            )
                        }
                    ) {
                        Text("Negotiate")
                    }
                }
            }
        }
    }
}

private fun formatMoney(value: Double): String =
    String.format(Locale.getDefault(), "%,.2f", value)

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale.getDefault(), "%.2f", value)
