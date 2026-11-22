package com.example.cti_cart.ui.screens

import androidx.navigation.NavController
import com.example.cti_cart.data.FirebaseRepository
import com.example.cti_cart.model.User

/** Map a stored role to a start route. Unknown or empty roles go to role selection. */
fun dashboardRouteForRole(role: String?): String {
    return when (role) {
        User.ROLE_SUPPLIER -> "supplier_dashboard"
        User.ROLE_BUYER -> "buyer_dashboard"
        else -> "role"
    }
}

/**
 * After splash/login/OTP/Google succeeds, send the user to the dashboard for
 * their saved role, or to role selection if none is stored yet.
 */
fun navigateAfterAuth(
    navController: NavController,
    popUpToRoute: String
) {
    val uid = FirebaseRepository.auth.currentUser?.uid
    if (uid == null) {
        navController.navigate("welcome") {
            popUpTo(popUpToRoute) { inclusive = true }
            launchSingleTop = true
        }
        return
    }

    FirebaseRepository.getUserRole(uid) { role ->
        navController.navigate(dashboardRouteForRole(role)) {
            popUpTo(popUpToRoute) { inclusive = true }
            launchSingleTop = true
        }
    }
}

/** Clear the nav graph and open [destination] (used by logout and Switch). */
fun clearStackAndNavigate(
    navController: NavController,
    destination: String
) {
    navController.navigate(destination) {
        popUpTo(navController.graph.id) { inclusive = false }
        launchSingleTop = true
    }
}
