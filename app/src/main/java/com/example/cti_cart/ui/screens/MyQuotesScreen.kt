package com.example.cti_cart.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.Quote
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun MyQuotesScreen(navController: NavController) {
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        var listener: ListenerRegistration? = null
        listener = FirebaseRepository.listenToMyQuotes(
            onResult = {
                quotes = it
                isLoading = false
                errorMessage = null
            },
            onFailure = {
                isLoading = false
                errorMessage = it.message ?: "Unable to load submitted quotes."
            }
        )

        onDispose {
            listener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                "My Quotes",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        when {
            isLoading -> CircularProgressIndicator()
            quotes.isEmpty() -> Text("No submitted quotes yet")
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotes, key = { it.id }) { quote ->
                    MyQuoteCard(quote)
                }
            }
        }
    }
}

@Composable
private fun MyQuoteCard(quote: Quote) {
    val statusColor = when (quote.status.lowercase()) {
        "accepted" -> Color(0xFF2E7D32)
        "rejected" -> Color(0xFFC62828)
        "negotiation", "negotiating" -> Color(0xFFEF6C00)
        else -> Color(0xFF6A4FB3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F0F7)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = quote.partName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = quote.status,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Quantity: ${formatNumber(quote.quantity)}")
            Text("Unit Price: ₹${formatMoney(quote.unitPrice)}")
            Text("Total Amount: ₹${formatMoney(quote.totalAmount)}")
            Text("Delivery: ${quote.deliveryTime}")
            Text("Validity: ${quote.validity}")
            Text("Payment: ${quote.paymentTerms}")
        }
    }
}

private fun formatMoney(value: Double): String =
    String.format("%,.2f", value)

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format("%.2f", value)
