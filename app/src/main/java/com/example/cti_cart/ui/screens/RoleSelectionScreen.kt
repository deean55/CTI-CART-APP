package com.example.cti_cart.ui.screens


import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.model.User

@Composable
fun RoleSelectionScreen(navController: NavController) {

    val context = LocalContext.current

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
                saveRoleAndNavigate(User.ROLE_SUPPLIER, navController, context)
            }
        )

        RoleCard(
            title = "Job Provider / Buyer",
            onClick = {
                saveRoleAndNavigate(User.ROLE_BUYER, navController, context)
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

fun saveRoleAndNavigate(
    role: String,
    navController: NavController,
    context: android.content.Context
) {
    FirebaseRepository.saveUserRole(
        role = role,
        onSuccess = {
            navController.navigate(dashboardRouteForRole(role)) {
                popUpTo("role") { inclusive = true }
            }
        },
        onFailure = { error ->
            Toast.makeText(
                context,
                "Could not save role: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    )
}
