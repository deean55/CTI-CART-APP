package com.example.cti_cart.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.cti_cart.model.Machine

@Composable
fun MachineCard(machine: Machine) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
        ) {

            // MACHINE IMAGE
            AsyncImage(
                model = machine.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .border(1.dp, Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // DETAILS
            Column {
                Text("Machine Name: ${machine.name}")
                Text("Hourly Rate: ₹${machine.hourlyRate}")
                Text("Utilization: ${machine.utilization}%")
            }
        }
    }
}