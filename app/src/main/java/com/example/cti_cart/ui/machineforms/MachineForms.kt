package com.example.cti_cart.ui.machineforms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp



@Composable
fun VMCForm(
    xTravel: String,
    onXTravelChange: (String) -> Unit,

    yTravel: String,
    onYTravelChange: (String) -> Unit,

    zTravel: String,
    onZTravelChange: (String) -> Unit,

    spindleTaper: String,
    onSpindleTaperChange: (String) -> Unit,

    controlSystem: String,
    onControlSystemChange: (String) -> Unit,

    axisCount: String,
    onAxisCountChange: (String) -> Unit
) {

    OutlinedTextField(
        value = xTravel,
        onValueChange = onXTravelChange,
        label = { Text("X Travel (mm)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = yTravel,
        onValueChange = onYTravelChange,
        label = { Text("Y Travel (mm)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = zTravel,
        onValueChange = onZTravelChange,
        label = { Text("Z Travel (mm)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = spindleTaper,
        onValueChange = onSpindleTaperChange,
        label = { Text("Spindle Taper") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = controlSystem,
        onValueChange = onControlSystemChange,
        label = { Text("Control System") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = axisCount,
        onValueChange = onAxisCountChange,
        label = { Text("Axis Count") },
        modifier = Modifier.fillMaxWidth()
    )
}
// HMC FORM

@Composable
fun HMCForm(
    xTravel: String,
    onXTravelChange: (String) -> Unit,

    yTravel: String,
    onYTravelChange: (String) -> Unit,

    zTravel: String,
    onZTravelChange: (String) -> Unit,

    spindleTaper: String,
    onSpindleTaperChange: (String) -> Unit,

    controlSystem: String,
    onControlSystemChange: (String) -> Unit,

    axisCount: String,
    onAxisCountChange: (String) -> Unit,

    palletSize: String,
    onPalletSizeChange: (String) -> Unit,

    numberOfPallets: String,
    onNumberOfPalletsChange: (String) -> Unit,

    bAxis: Boolean,
    onBAxisChange: (Boolean) -> Unit,

    bAxisDegree: String,
    onBAxisDegreeChange: (String) -> Unit
) {

    // Reuse VMC fields
    VMCForm(
        xTravel = xTravel,
        onXTravelChange = onXTravelChange,

        yTravel = yTravel,
        onYTravelChange = onYTravelChange,

        zTravel = zTravel,
        onZTravelChange = onZTravelChange,

        spindleTaper = spindleTaper,
        onSpindleTaperChange = onSpindleTaperChange,

        controlSystem = controlSystem,
        onControlSystemChange = onControlSystemChange,

        axisCount = axisCount,
        onAxisCountChange = onAxisCountChange
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "HMC Specific",
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = palletSize,
        onValueChange = onPalletSizeChange,
        label = { Text("Pallet Size (mm)") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = numberOfPallets,
        onValueChange = onNumberOfPalletsChange,
        label = { Text("Number of Pallets") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text("B Axis")

        Switch(
            checked = bAxis,
            onCheckedChange = onBAxisChange
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = bAxisDegree,
        onValueChange = onBAxisDegreeChange,
        label = { Text("B Axis Degree") },
        modifier = Modifier.fillMaxWidth()
    )
}