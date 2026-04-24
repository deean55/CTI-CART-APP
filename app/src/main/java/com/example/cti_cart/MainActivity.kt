package com.example.cti_cart

import com.example.cti_cart.ui.screens.RegisterScreen
import com.example.cti_cart.ui.screens.RoleSelectionScreen
import com.example.cti_cart.ui.screens.SplashScreen
import com.example.cti_cart.ui.screens.WelcomeScreen
import com.example.cti_cart.ui.screens.LoginScreen


import com.example.cti_cart.ui.screens.OtpLoginScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.*
import com.example.cti_cart.ui.screens.AddDetailsScreen
import com.example.cti_cart.ui.screens.AddMachineScreen
import com.example.cti_cart.ui.screens.BuyerDashboardScreen
import com.example.cti_cart.ui.screens.MapPickerScreen
import com.example.cti_cart.ui.screens.PostRFQScreen
import com.example.cti_cart.ui.screens.SupplierDashboardScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash") {

                composable("splash") {
                    SplashScreen(navController)
                }

                composable("welcome") {
                    WelcomeScreen(navController)
                }

                composable("login") {
                    LoginScreen(navController)
                }

                composable("register") {
                    RegisterScreen(navController)
                }
                composable("mapPicker") {
                    MapPickerScreen(navController)
                }

                composable("role") {
                    RoleSelectionScreen(navController)
                }

                composable("supplier_dashboard") {
                    SupplierDashboardScreen(navController)
                }
                composable("add_details") {
                    AddDetailsScreen(navController)
                }
                composable("buyer_dashboard") {
                    BuyerDashboardScreen(navController)}
                // -------- ADD MACHINE --------
                composable("add_machine") {
                    AddMachineScreen(navController)
                }
                // -------- EDIT MACHINE (IMPORTANT) --------
                composable("add_machine/{machineId}") { backStackEntry ->

                    val machineId = backStackEntry.arguments?.getString("machineId")

                    AddMachineScreen(
                        navController = navController,
                        machineId = machineId
                    )
                }
                composable("post_rfq") {
                    PostRFQScreen(navController)
                }
                composable("post_job") {
                    PostRFQScreen(navController)
                }
                composable("upload_machine") { /* UploadMachineScreen() */ }
                composable("view_machines") { /* ViewMachinesScreen() */ }
                composable("upload_certificate") { /* UploadCertificateScreen() */ }
                composable("view_certificates") { /* ViewCertificatesScreen() */ }
                composable("posted_jobs") { /* PostedJobsScreen() */ }

                composable("otp") {
                    OtpLoginScreen(navController)
                }

            }
        }
    }
}
