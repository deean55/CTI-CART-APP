package com.example.cti_cart.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cti_cart.data.FirebaseRepository
import androidx.compose.ui.Alignment

@Composable
fun MachineListSection() {

    var machines by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getMachinesBySupplier {
            machines = it
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        machines.isEmpty() -> {
            Text("No machines added yet")
        }

        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                machines.forEach { machine ->
                    MachineRow(machine)
                }
            }
        }
    }
}

@Composable
fun MachineRow(machine: Map<String, Any>) {

    val name = machine["name"] as? String ?: ""
    val rate = machine["hourlyRate"] as? String ?: ""
    val utilization = machine["utilization"] as? String ?: ""

    val imageUrl = when {
        machine["imageUrl"] != null -> machine["imageUrl"] as String
        machine["images"] is List<*> -> (machine["images"] as List<*>).firstOrNull() as? String
        else -> null
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // LEFT: IMAGE BOX
        Box(
            modifier = Modifier
                .size(100.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No Image", modifier = Modifier.align(Alignment.Center))
            }
        }

        // RIGHT: DETAILS BOX
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .padding(8.dp)
        ) {
            Text("Machine Name: $name")
            Text("Hourly Rate: $rate")
            Text("Utilization: $utilization%")
        }
    }
}