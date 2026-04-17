package com.example.cti_cart.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun MapPickerScreen(navController: NavController) {

    val context = LocalContext.current
    var markerPosition by remember {
        mutableStateOf(LatLng(12.9716, 77.5946))
    }

    var address by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 14f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerPosition = latLng

                val geocoder = Geocoder(context, Locale.getDefault())
                val result = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1
                )

                address = result?.firstOrNull()?.getAddressLine(0) ?: ""
            }
        ) {

            Marker(
                state = MarkerState(position = markerPosition),
                title = "Selected Location"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Card {
                Column(modifier = Modifier.padding(12.dp)) {

                    Text(address)

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_location", address)

                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONFIRM LOCATION")
                    }
                }
            }
        }
    }
}