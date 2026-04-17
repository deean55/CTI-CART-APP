package com.example.cti_cart.ui.screens

import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cti_cart.data.FirebaseRepository
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AddMachineScreen(navController: NavController) {

    val context = LocalContext.current

    // ---------------- STATE ----------------

    var machineName by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var utilization by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
            .statusBarsPadding()
            .padding(horizontal = 20.dp) // FIXED top padding issue
    ) {

        Spacer(modifier = Modifier.height(12.dp)) // controlled spacing

        // -------- TITLE --------
        Text(
            text = "Add Machine",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------- INPUTS --------

        OutlinedTextField(
            value = machineName,
            onValueChange = { machineName = it },
            label = { Text("Machine Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = hourlyRate,
            onValueChange = { hourlyRate = it },
            label = { Text("Hourly Rate") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = utilization,
            onValueChange = { utilization = it },
            label = { Text("Utilization in %") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -------- IMAGE BUTTON --------

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Machine Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -------- IMAGE PREVIEW --------

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f)) // pushes button to bottom

        // -------- SUBMIT BUTTON --------

        Button(
            onClick = {
                // Check if USer id logged.
                Log.d("AUTH", FirebaseAuth.getInstance().currentUser?.uid ?: "NULL USER")
                // -------- VALIDATION --------

                if (machineName.isBlank() || hourlyRate.isBlank() || utilization.isBlank()) {
                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (imageUri == null) {
                    Toast.makeText(context, "Select an image", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                // -------- FIREBASE CALL --------

                FirebaseRepository.uploadMachineWithImage(
                    name = machineName.trim(),
                    rate = hourlyRate.trim(),
                    utilization = utilization.trim(),
                    imageUri = imageUri!!,

                    onSuccess = {
                        isLoading = false

                        Toast.makeText(context, "Machine Added", Toast.LENGTH_SHORT).show()

                        // RESET FORM
                        machineName = ""
                        hourlyRate = ""
                        utilization = ""
                        imageUri = null
                    },

                    onFailure = {
                        isLoading = false
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(50)
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                   Text("SUBMIT")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}