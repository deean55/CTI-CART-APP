package com.example.cti_cart.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@Composable
fun OtpLoginScreen(navController: NavController) {

    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isNavigated by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Login with OTP", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            placeholder = { Text("+91XXXXXXXXXX") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {

                if (phone.isBlank() || !phone.startsWith("+")) {
                    Toast.makeText(context, "Enter valid phone number", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (activity == null) {
                    Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object :
                        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(
                            credential: PhoneAuthCredential
                        ) {
                            signIn(
                                auth,
                                credential,
                                context,
                                navController,
                                onDone = { isLoading = false },
                                isNavigated = isNavigated,
                                setNavigated = { isNavigated = true }
                            )
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onCodeSent(
                            verId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            isLoading = false
                            verificationId = verId
                            Toast.makeText(context, "OTP Sent", Toast.LENGTH_SHORT).show()
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Sending..." else "Send OTP")
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("Enter OTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {

                if (verificationId.isBlank()) {
                    Toast.makeText(context, "Request OTP first", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (otp.length < 6) {
                    Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val credential = PhoneAuthProvider.getCredential(
                    verificationId,
                    otp
                )

                signIn(
                    auth,
                    credential,
                    context,
                    navController,
                    isNavigated = isNavigated,
                    setNavigated = { isNavigated = true }
                )

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify OTP")
        }
    }
}

fun signIn(
    auth: FirebaseAuth,
    credential: PhoneAuthCredential,
    context: android.content.Context,
    navController: NavController,
    onDone: (() -> Unit)? = null,
    isNavigated: Boolean,
    setNavigated: () -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener {

            onDone?.invoke()

            if (it.isSuccessful && !isNavigated) {

                setNavigated()

                Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()

                navController.navigate("role") {
                    popUpTo("otp") { inclusive = true }
                    launchSingleTop = true
                }

            } else if (!it.isSuccessful) {

                Toast.makeText(
                    context,
                    "Invalid OTP: ${it.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}