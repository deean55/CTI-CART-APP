package com.example.cti_cart.ui.screens


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.*
import com.google.firebase.firestore.SetOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RoleSelectionScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Your Role",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoleCard(
            title = "Machine Owner / Supplier",
            onClick = {
                navController.navigate("supplier_dashboard") {
                    popUpTo("role") { inclusive = true }
                }
            }
        )

        RoleCard(
            title = "Job Provider / Buyer",
            onClick = {
                navController.navigate("buyer_dashboard") {
                    popUpTo("role") { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun RoleCard(title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("CONTINUE")
            }
        }
    }
}

fun saveRoleAndNavigate(role: String, navController: NavController) {

    val user = FirebaseAuth.getInstance().currentUser ?: return

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(user.uid)
        .set(
            mapOf(
                "role" to role,
                "email" to user.email
            ),SetOptions.merge()
        )
        .addOnSuccessListener {

            navController.navigate("supplier_dashboard") {
                popUpTo("role") { inclusive = true }
            }

        }
}

