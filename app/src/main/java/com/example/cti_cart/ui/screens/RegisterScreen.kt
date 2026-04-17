package com.example.cti_cart.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import java.util.*
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.model.User


@Composable
fun RegisterScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var registering by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getLocation(context, fusedLocationClient) { address, lat, lng ->
                location = address
                latitude = lat
                longitude = lng
                loading = false
            }
        } else {
            loading = false
        }
    }
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<String>("selected_location")
            ?.observeForever { selected ->
                location = selected
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
    ) {

        Text("Register", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(company, { company = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("mapPicker")
                }
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = {},
                label = { Text("Select Location") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                trailingIcon = {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = {

                            loading = true

                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                getLocation(context, fusedLocationClient) { address, lat, lng ->
                                    location = address
                                    latitude = lat
                                    longitude = lng
                                    loading = false
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }

                        }) {
                            Icon(Icons.Default.MyLocation, "Auto Detect")
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Add Progress Indicator UI
        if (registering) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(

            onClick = {

                if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                    println("Please fill all fields")
                    return@Button
                }

                registering = true
                val auth = FirebaseRepository.auth

                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->

                        val uid = result.user!!.uid

                        val user = User(
                            fullName = name,
                            phone = phone,
                            email = email,
                            companyName = company,
                            address = location,
                            city = location,
                            latitude = latitude,
                            longitude = longitude,
                            role = "customer"
                        )

                        FirebaseRepository.firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                registering = false   // 👈 add here
                                navController.navigate("dashboard")
                            }
                            .addOnFailureListener {
                                registering = false
                                Toast.makeText(
                                    context,
                                    "Database save failed: ${it.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener {
                        registering = false   // 👈 add here
                        Toast.makeText(
                            context,
                            "Register failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTER")
        }
    }
}
@SuppressLint("MissingPermission")
fun getLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onResult: (String, Double, Double) -> Unit
) {

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        onResult("Enable GPS and try again", 0.0, 0.0)
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { loc ->

            if (loc != null) {

                val geocoder = Geocoder(context, Locale.getDefault())
                val address = geocoder.getFromLocation(
                    loc.latitude,
                    loc.longitude,
                    1
                )?.firstOrNull()?.getAddressLine(0)
                    ?: "${loc.latitude}, ${loc.longitude}"

                onResult(address, loc.latitude, loc.longitude)

            } else {
                // fallback to current location
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { current ->

                    current?.let {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val address = geocoder.getFromLocation(
                            it.latitude,
                            it.longitude,
                            1
                        )?.firstOrNull()?.getAddressLine(0)
                            ?: "${it.latitude}, ${it.longitude}"

                        onResult(address, it.latitude, it.longitude)
                    }
                }
            }
        }
}

