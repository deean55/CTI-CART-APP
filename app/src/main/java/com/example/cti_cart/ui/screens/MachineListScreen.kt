package com.example.cti_cart.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cti_cart.data.FirebaseRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

// -------------------- MACHINE LIST SECTION --------------------

@Composable
fun MachineListSection(navController: NavController) {

    var machines by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current

    fun loadMachines() {
        isLoading = true
        FirebaseRepository.getMachinesBySupplier {
            machines = it
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadMachines()
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                machines.forEach { machine ->

                    MachineRow(
                        machine = machine,
                        navController = navController,
                        onDelete = {

                            val id = machine["id"] as? String ?: return@MachineRow

                            val imageUrl = when {
                                machine["imageUrl"] != null -> machine["imageUrl"] as String
                                machine["images"] is List<*> -> (machine["images"] as List<*>).firstOrNull() as? String
                                else -> null
                            }

                            FirebaseRepository.deleteMachine(
                                documentId = id,
                                imageUrl = imageUrl,
                                onSuccess = {
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                    loadMachines()
                                },
                                onFailure = {
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

// -------------------- MACHINE ROW --------------------

@Composable
fun MachineRow(
    machine: Map<String, Any>,
    navController: NavController,
    onDelete: () -> Unit
) {

    val name = machine["name"] as? String ?: ""
    val rate = machine["hourlyRate"] as? String ?: ""
    val utilization = machine["utilization"] as? String ?: ""

    val imageUrl = when {
        machine["imageUrl"] != null -> machine["imageUrl"] as String
        machine["images"] is List<*> -> (machine["images"] as List<*>).firstOrNull() as? String
        else -> null
    }

    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -------- IMAGE --------
        Box(
            modifier = Modifier
                .size(90.dp)
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

        // -------- DETAILS --------
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

        // -------- EDIT BUTTON --------
        IconButton(onClick = {
            val id = machine["id"] as? String ?: return@IconButton
            navController.navigate("add_machine/$id") // 🔥 EDIT FLOW
        }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }

        // -------- DELETE BUTTON --------
        IconButton(onClick = {
            showDialog = true
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    // -------- CONFIRM DELETE DIALOG --------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Machine") },
            text = { Text("Are you sure you want to delete this machine?") },

            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },

            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}