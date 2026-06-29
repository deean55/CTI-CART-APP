package com.example.cti_cart.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AddMachineScreen(
    navController: NavController,
    machineId: String? = null // 🔥 NULL = ADD | NOT NULL = EDIT
) {

    val context = LocalContext.current

    var machineName by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var utilization by remember { mutableStateOf("") }

    // Basic Details
    var machineType by remember { mutableStateOf("") }

// Travel Size
    var xTravel by remember { mutableStateOf("") }
    var yTravel by remember { mutableStateOf("") }
    var zTravel by remember { mutableStateOf("") }

// Dropdowns
    var spindleTaper by remember { mutableStateOf("BT40") }
    var controlSystem by remember { mutableStateOf("Fanuc") }
    var axisCount by remember { mutableStateOf("3 Axis") }

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

                    machineType = it.getString("machineType") ?: ""

                    xTravel = (it.getLong("xTravel") ?: 0L).toString()
                    yTravel = (it.getLong("yTravel") ?: 0L).toString()
                    zTravel = (it.getLong("zTravel") ?: 0L).toString()

                    spindleTaper = it.getString("spindleTaper") ?: "BT40"
                    controlSystem = it.getString("controlSystem") ?: "Fanuc"
                    axisCount = it.getString("axisCount") ?: "3 Axis"

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 20.dp)
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

        OutlinedTextField(
            value = machineType,
            onValueChange = { machineType = it },
            label = { Text("Machine Type") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = xTravel,
            onValueChange = { xTravel = it },
            label = { Text("X Travel (mm)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = yTravel,
            onValueChange = { yTravel = it },
            label = { Text("Y Travel (mm)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = zTravel,
            onValueChange = { zTravel = it },
            label = { Text("Z Travel (mm)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = spindleTaper,
            onValueChange = { spindleTaper = it },
            label = { Text("Spindle Taper") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = controlSystem,
            onValueChange = { controlSystem = it },
            label = { Text("Control System") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = axisCount,
            onValueChange = { axisCount = it },
            label = { Text("Axis Count") },
            modifier = Modifier.fillMaxWidth()
        )
        val spindleTaperOptions = listOf(
            "BT30",
            "BT40",
            "BT50",
            "CAT40",
            "CAT50",
            "HSK63",
            "HSK100"
        )
        Spacer(modifier = Modifier.height(16.dp))
        val controlSystemOptions = listOf(
            "Fanuc",
            "Siemens",
            "Mitsubishi",
            "Heidenhain",
            "Mazatrol",
            "Fagor",
            "Haas",
            "Syntec",
            "Other"
        )
        Spacer(modifier = Modifier.height(16.dp))
        val axisCountOptions = listOf(
            "2 Axis",
            "3 Axis",
            "4 Axis",
            "5 Axis",
            "6 Axis"
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

                                updateMachine(
                                    machineId = machineId!!,
                                    name = machineName,
                                    rate = hourlyRate,
                                    utilization = utilization,

                                    machineType = machineType,

                                    xTravel = xTravel,
                                    yTravel = yTravel,
                                    zTravel = zTravel,

                                    spindleTaper = spindleTaper,
                                    controlSystem = controlSystem,
                                    axisCount = axisCount,

                                    imageUrl = existingImageUrl,
                                    context = context
                                ) {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Machine Updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onFailure = {
                                isLoading = false
                            }
                        )
                    } else {
                        // No new image → keep old
                        updateMachine(
                            machineId = machineId!!,
                            name = machineName,
                            rate = hourlyRate,
                            utilization = utilization,

                            machineType = machineType,

                            xTravel = xTravel,
                            yTravel = yTravel,
                            zTravel = zTravel,

                            spindleTaper = spindleTaper,
                            controlSystem = controlSystem,
                            axisCount = axisCount,

                            imageUrl = existingImageUrl,
                            context = context
                        )  {
                            isLoading = false
                            Toast.makeText(context, "Machine Updated", Toast.LENGTH_SHORT).show()
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

                        machineType = machineType,

                        xTravel = xTravel,
                        yTravel = yTravel,
                        zTravel = zTravel,

                        spindleTaper = spindleTaper,
                        controlSystem = controlSystem,
                        axisCount = axisCount,

                        imageUri = imageUri!!,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Machine Added", Toast.LENGTH_SHORT).show()
                            machineName = ""
                            hourlyRate = ""
                            utilization = ""
                            imageUri = null
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
        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Added Machines",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        MachineListSection(navController)
    }
}

// ---------------- UPDATE FUNCTION ----------------

fun updateMachine(
    machineId: String,
    name: String,
    rate: String,
    utilization: String,

    machineType: String,

    xTravel: String,
    yTravel: String,
    zTravel: String,

    spindleTaper: String,
    controlSystem: String,
    axisCount: String,

    imageUrl: String?,
    context: Context,
    onDone: () -> Unit
) {

    val data = mutableMapOf<String, Any>(
        "name" to name,
        "hourlyRate" to rate,
        "utilization" to utilization,

        "machineType" to machineType,

        "xTravel" to (xTravel.toIntOrNull() ?: 0),
        "yTravel" to (yTravel.toIntOrNull() ?: 0),
        "zTravel" to (zTravel.toIntOrNull() ?: 0),

        "spindleTaper" to spindleTaper,
        "controlSystem" to controlSystem,
        "axisCount" to axisCount
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