package com.example.cti_cart.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Composable
fun PostRFQScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fileUri = it
            fileName = it.lastPathSegment ?: "Selected File"
        }
    }

    // ✅ Upload function FIXED (inside composable)
    fun uploadFile(
        uri: Uri?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (uri == null) {
            onSuccess("")
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("rfq_files/${UUID.randomUUID()}")

        fileRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload failed")
                }
                fileRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }
            .addOnFailureListener {
                onError(it.message ?: "Upload failed")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()) // ✅ prevents overflow
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Text("Post Requirement", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Requirement Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Budget (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📎 Attach File Button
        OutlinedButton(
            onClick = { launcher.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Attach Drawing / File")
        }

        if (fileName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Selected: $fileName", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {

                if (title.isBlank() || description.isBlank()) {
                    error = "Please fill all required fields"
                    return@Button
                }

                isLoading = true
                error = ""

                // ✅ Upload first, then save RFQ
                uploadFile(
                    uri = fileUri,
                    onSuccess = { fileUrl ->

                        val rfq = hashMapOf(
                            "title" to title,
                            "description" to description,
                            "quantity" to quantity,
                            "budget" to budget,
                            "location" to location,
                            "fileUrl" to fileUrl, // ✅ ADDED
                            "createdBy" to userId,
                            "status" to "open",
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("rfqs")
                            .add(rfq)
                            .addOnSuccessListener {
                                isLoading = false
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                error = it.message ?: "Failed to post RFQ"
                            }
                    },
                    onError = {
                        isLoading = false
                        error = it
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Submit RFQ")
            }
        }
    }
}