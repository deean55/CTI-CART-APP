package com.example.cti_cart.ui.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.cti_cart.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import androidx.compose.ui.res.stringResource
// Test change for feature branch
@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    val googleSignInOptions = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                // 🔥 DEBUG LOG
                Log.d("GOOGLE_LOGIN", "ID TOKEN: ${account.idToken}")

                if (account.idToken == null) {
                    Toast.makeText(
                        context,
                        "ID Token is NULL → Check Web Client ID",
                        Toast.LENGTH_LONG
                    ).show()
                    return@rememberLauncherForActivityResult
                }

                firebaseAuthWithGoogle(account.idToken!!, navController)

            } catch (e: ApiException) {

                Log.e("GOOGLE_LOGIN", "Google sign in failed", e)

                Toast.makeText(
                    context,
                    "Google Sign-In failed: ${e.statusCode}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
    ) {

        Text(
            "Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        context,
                        "Enter email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            navController.navigate("role") {
                                popUpTo("login") { inclusive = true }
                            }

                        } else {

                            Toast.makeText(
                                context,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("otp")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login with OTP")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login with Google")
        }
    }
}

fun firebaseAuthWithGoogle(idToken: String, navController: NavController) {

    val context = navController.context
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    FirebaseAuth.getInstance()
        .signInWithCredential(credential)
        .addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Log.d("FIREBASE_AUTH", "SUCCESS")

                Toast.makeText(
                    context,
                    "Google Login Success",
                    Toast.LENGTH_SHORT
                ).show()

                navController.navigate("role") {
                    popUpTo("login") { inclusive = true }
                }

            } else {

                Log.e("FIREBASE_AUTH", "FAILED", task.exception)

                Toast.makeText(
                    context,
                    "Firebase failed: ${task.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}