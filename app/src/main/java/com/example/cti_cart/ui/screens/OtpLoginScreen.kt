package com.example.cti_cart.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@Composable
fun OtpLoginScreen(navController: NavController) {

    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember {
        mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null)
    }

    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isNavigated by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()

    /*
     * Convert the entered Indian phone number into E.164 format.
     *
     * Accepted:
     * 9876543210
     * +919876543210
     * 919876543210
     * +91 9876543210
     */
    fun getIndianPhoneNumber(input: String): String? {

        val cleaned = input
            .trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        return when {

            // 10 digit Indian number
            cleaned.matches(Regex("[6-9][0-9]{9}")) ->
                "+91$cleaned"

            // +91 followed by 10 digits
            cleaned.matches(Regex("\\+91[6-9][0-9]{9}")) ->
                cleaned

            // 91 followed by 10 digits
            cleaned.matches(Regex("91[6-9][0-9]{9}")) ->
                "+$cleaned"

            else -> null
        }
    }

    /*
     * Send OTP
     */
    fun sendOtp(
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {

        val phoneNumber = getIndianPhoneNumber(phone)

        if (phoneNumber == null) {
            Toast.makeText(
                context,
                "Enter a valid 10-digit Indian phone number",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (activity == null) {
            Toast.makeText(
                context,
                "Activity not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Show the normalized number in the field
        phone = phoneNumber

        isSendingOtp = true

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    /*
                     * Automatic verification
                     */
                    override fun onVerificationCompleted(
                        credential: PhoneAuthCredential
                    ) {

                        if (isNavigated) return

                        signInWithPhoneCredential(
                            auth = auth,
                            credential = credential,
                            context = context,
                            navController = navController,
                            onSuccess = {
                                isSendingOtp = false
                                isVerifyingOtp = false
                                isNavigated = true
                            }
                        )
                    }

                    /*
                     * Verification failed
                     */
                    override fun onVerificationFailed(
                        e: FirebaseException
                    ) {

                        isSendingOtp = false

                        Toast.makeText(
                            context,
                            "OTP Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    /*
                     * OTP successfully sent
                     */
                    override fun onCodeSent(
                        verificationIdFromFirebase: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {

                        isSendingOtp = false
                        isOtpSent = true

                        verificationId = verificationIdFromFirebase
                        resendToken = token

                        Toast.makeText(
                            context,
                            "OTP sent to $phoneNumber",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

        if (forceResendingToken != null) {
            optionsBuilder.setForceResendingToken(forceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(
            optionsBuilder.build()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Login with OTP",
            fontSize = 22.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        /*
         * PHONE NUMBER
         */
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },
            label = {
                Text("Phone Number")
            },
            placeholder = {
                Text("9876543210")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            enabled = !isSendingOtp && !isOtpSent
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * SEND OTP
         */
        Button(
            onClick = {
                sendOtp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isSendingOtp && !isOtpSent
        ) {

            Text(
                if (isSendingOtp) {
                    "Sending OTP..."
                } else {
                    "SEND OTP"
                }
            )
        }

        /*
         * OTP SECTION
         */
        if (isOtpSent) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Enter the 6-digit OTP",
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otp = it
                    }
                },
                label = {
                    Text("Enter OTP")
                },
                placeholder = {
                    Text("123456")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            /*
             * VERIFY OTP
             */
            Button(
                onClick = {

                    if (otp.length != 6) {

                        Toast.makeText(
                            context,
                            "Enter the 6-digit OTP",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    val currentVerificationId = verificationId

                    if (currentVerificationId.isNullOrBlank()) {

                        Toast.makeText(
                            context,
                            "Please request OTP first",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    isVerifyingOtp = true

                    val credential =
                        PhoneAuthProvider.getCredential(
                            currentVerificationId,
                            otp
                        )

                    signInWithPhoneCredential(
                        auth = auth,
                        credential = credential,
                        context = context,
                        navController = navController,
                        onSuccess = {
                            isVerifyingOtp = false
                            isNavigated = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isVerifyingOtp && !isNavigated
            ) {

                Text(
                    if (isVerifyingOtp) {
                        "VERIFYING..."
                    } else {
                        "VERIFY OTP"
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            /*
             * RESEND OTP
             */
            OutlinedButton(
                onClick = {

                    otp = ""

                    sendOtp(
                        forceResendingToken = resendToken
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSendingOtp
            ) {

                Text(
                    if (isSendingOtp) {
                        "Sending..."
                    } else {
                        "RESEND OTP"
                    }
                )
            }
        }
    }
}


/*
 * Firebase phone authentication
 */
fun signInWithPhoneCredential(
    auth: FirebaseAuth,
    credential: PhoneAuthCredential,
    context: android.content.Context,
    navController: NavController,
    onSuccess: () -> Unit
) {

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Toast.makeText(
                    context,
                    "Login Success",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess()

                navController.navigate("role") {

                    popUpTo("otp") {
                        inclusive = true
                    }

                    launchSingleTop = true
                }

            } else {

                Toast.makeText(
                    context,
                    "OTP verification failed: ${
                        task.exception?.message
                    }",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}