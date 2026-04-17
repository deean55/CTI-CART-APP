package com.example.cti_cart.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cti_cart.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.cti_cart.data.FirebaseRepository


data class OnboardingPage(
    val image: Int,
    val title: String,
    val subtitle: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SplashScreen(navController: NavController) {

    val user = FirebaseRepository.auth.currentUser

    LaunchedEffect(Unit) {
        delay(800)

        if (user != null) {
            navController.navigate("role") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    val pages = listOf(
        OnboardingPage(
            R.drawable.onboard1,
            "CNC Job Marketplace",
            "Find machining capacity near you"
        ),
        OnboardingPage(
            R.drawable.onboard2,
            "Post Jobs Easily",
            "Connect with verified CNC vendors"
        ),
        OnboardingPage(
            R.drawable.onboard3,
            "Grow Your Business",
            "Increase machine utilization"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(pages[page].image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (page == pages.lastIndex) {
                        navController.navigate("welcome") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(page + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (page == pages.lastIndex) "GET STARTED" else "NEXT")
            }
        }
    }
}