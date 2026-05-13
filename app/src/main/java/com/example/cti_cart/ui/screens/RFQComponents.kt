package com.example.cti_cart.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RFQStatusBadge(status: String) {

    val bgColor = when (status) {
        "Quoted" -> Color(0xFF2E7D32)
        "Closed" -> Color(0xFFD32F2F)
        else -> Color(0xFFF57C00)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        shape = RoundedCornerShape(50)
    ) {

        Text(
            text = status,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}