package com.example.cti_cart.ui.screens

import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun DrawingViewerScreen(
    navController: NavController,
    fileUrl: String
) {

    var loadGoogleViewer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        // Header
        Row(modifier = Modifier.padding(12.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Drawing", style = MaterialTheme.typography.titleLarge)
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->

                WebView(context).apply {

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {

                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            // 🔥 If direct load fails → use Google viewer
                            loadGoogleViewer = true
                        }
                    }

                    // 🔥 Try direct load first
                    loadUrl(fileUrl)
                }
            },
            update = { webView ->
                if (loadGoogleViewer) {
                    webView.loadUrl(
                        "https://docs.google.com/gview?embedded=true&url=$fileUrl"
                    )
                }
            }
        )
    }
}