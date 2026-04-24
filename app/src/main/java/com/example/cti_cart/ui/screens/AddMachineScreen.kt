package com.example.cti_cart.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cti_cart.data.FirebaseRepository
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AddMachineScreen(
    navController: NavController,
    machineId: String? = null // 🔥 NULL = ADD | NOT NULL = EDIT
) {

    val context = LocalContext.current

    var machineName by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var utilization by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isEditMode = machineId != null

    // ---------------- LOAD DATA FOR EDIT ----------------

    LaunchedEffect(machineId) {
        if (machineId != null) {
            isLoading = true

            FirebaseRepository.firestore
                .collection("machines")
                .document(machineId)
                .get()
                .addOnSuccessListener {

                    machineName = it.getString("name") ?: ""
                    hourlyRate = it.getString("hourlyRate") ?: ""
                    utilization = it.getString("utilization") ?: ""

                    existingImageUrl =
                        it.getString("imageUrl")
                            ?: (it.get("images") as? List<*>)?.firstOrNull() as? String

                    isLoading = false
                }
        }
    }

    // ---------------- IMAGE PICKER ----------------

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // ---------------- UI ----------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = if (isEditMode) "Edit Machine" else "Add Machine",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = machineName,
            onValueChange = { machineName = it },
            label = { Text("Machine Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = hourlyRate,
            onValueChange = { hourlyRate = it },
            label = { Text("Hourly Rate") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = utilization,
            onValueChange = { utilization = it },
            label = { Text("Utilization %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------- IMAGE BUTTON --------

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Machine Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- IMAGE PREVIEW --------

        when {
            imageUri != null -> {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            existingImageUrl != null -> {
                Image(
                    painter = rememberAsyncImagePainter(existingImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -------- SUBMIT BUTTON --------

        Button(
            onClick = {

                if (machineName.isBlank() || hourlyRate.isBlank() || utilization.isBlank()) {
                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                // 🔥 EDIT MODE
                if (isEditMode) {

                    if (imageUri != null) {
                        // New image → upload first
                        FirebaseRepository.uploadImage(
                            uri = imageUri!!,
                            onSuccess = { newUrl ->

                                updateMachine(machineId!!, machineName, hourlyRate, utilization, newUrl, context) {
                                    isLoading = false
                                    navController.popBackStack()
                                }
                            },
                            onFailure = {
                                isLoading = false
                            }
                        )
                    } else {
                        // No new image → keep old
                        updateMachine(machineId!!, machineName, hourlyRate, utilization, existingImageUrl, context) {
                            isLoading = false
                            navController.popBackStack()
                        }
                    }

                } else {
                    // 🔥 ADD MODE
                    if (imageUri == null) {
                        Toast.makeText(context, "Select an image", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        return@Button
                    }

                    FirebaseRepository.uploadMachineWithImage(
                        name = machineName,
                        rate = hourlyRate,
                        utilization = utilization,
                        imageUri = imageUri!!,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Machine Added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = {
                            isLoading = false
                        }
                    )
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(if (isEditMode) "UPDATE" else "SUBMIT")
            }
        }
    }
}

// ---------------- UPDATE FUNCTION ----------------

fun updateMachine(
    machineId: String,
    name: String,
    rate: String,
    utilization: String,
    imageUrl: String?,
    context: android.content.Context,
    onDone: () -> Unit
) {
    val data = mutableMapOf<String, Any>(
        "name" to name,
        "hourlyRate" to rate,
        "utilization" to utilization
    )

    imageUrl?.let {
        data["imageUrl"] = it
        data["images"] = listOf(it)
    }

    FirebaseRepository.firestore
        .collection("machines")
        .document(machineId)
        .update(data)
        .addOnSuccessListener {
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
            onDone()
        }
}