package com.example.cti_cart.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.data.model.RFQ
import java.util.*
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun PostRFQScreen(navController: NavController) {

    val context = LocalContext.current

    var partName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var requiredBy by remember { mutableStateOf("") }
    var selectedMachine by remember { mutableStateOf("CNC Turning") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    val machineOptions = listOf(
        "CNC Turning", "VMC", "Laser Cutting", "Grinding", "Sheet Metal"
    )

    var expanded by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            requiredBy = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // 🔥 fixes top overlap (important)
            .padding(horizontal = 16.dp, vertical = 12.dp) // cleaner than single 16dp
            .verticalScroll(rememberScrollState())
    ) {

        // Header
        Row {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Post Job", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = partName,
            onValueChange = { partName = it },
            label = { Text("Part Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = requiredBy,
            onValueChange = {},
            label = { Text("Required By") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Text("📅")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dropdown
        Box {
            OutlinedTextField(
                value = selectedMachine,
                onValueChange = {},
                readOnly = true,
                label = { Text("Machine Required") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                machineOptions.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            selectedMachine = it
                            expanded = false
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Upload
        Button(
            onClick = { filePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Drawing (PDF / Image / STEP / DXF)")
        }

        selectedFileUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("File Selected")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit
        Button(
            onClick = {
                Log.d("USER", FirebaseRepository.auth.currentUser?.uid ?: "NULL")
                // 🔥 FIX: Auth check
                if (FirebaseRepository.auth.currentUser == null) {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
                    return@Button
                }

                isLoading = true

                if (selectedFileUri != null) {

                    FirebaseRepository.uploadFile(
                        uri = selectedFileUri!!,
                        onSuccess = { fileUrl ->

                            val rfq = RFQ(
                                partName = partName,
                                quantity = quantity,
                                machine = selectedMachine,
                                requiredBy = requiredBy,
                                fileUrl = fileUrl
                            )

                            FirebaseRepository.saveRFQ(
                                rfq,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "RFQ Posted", Toast.LENGTH_SHORT).show()

                                    navController.navigate("my_rfqs") {
                                        popUpTo("post_rfq") { inclusive = true }
                                    }
                                },
                                onFailure = {
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onFailure = {
                            isLoading = false
                            Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )

                } else {

                    val rfq = RFQ(
                        partName = partName,
                        quantity = quantity,
                        machine = selectedMachine,
                        requiredBy = requiredBy
                    )

                    FirebaseRepository.saveRFQ(
                        rfq,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "RFQ Posted", Toast.LENGTH_SHORT).show()

                            navController.navigate("my_rfqs") {
                                popUpTo("post_rfq") { inclusive = true }
                            }
                        },
                        onFailure = {
                            isLoading = false
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = partName.isNotEmpty() && quantity.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("POST JOB")
            }
        }
    }
}