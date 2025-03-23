package com.insecureshop

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.insecureshop.util.Prefs
import kotlinx.android.synthetic.main.activity_product_list.*

class WebViewActivity : AppCompatActivity() {

    // Define allowed domains
    private val allowedDomains = listOf(
        "insecureshopapp.com",
        "www.insecureshopapp.com"
    )

    val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.65 Mobile Safari/537.36"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)

        val webview = findViewById<WebView>(R.id.webview)

        // Configure WebView with secure settings
        webview.settings.javaScriptEnabled = true // Only if needed by the app
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true
        webview.settings.allowUniversalAccessFromFileURLs = false // Disable this insecure feature
        webview.settings.allowFileAccessFromFileURLs = false // Disable file access
        webview.settings.userAgentString = USER_AGENT
        
        // Use a secure WebViewClient
        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                // Don't ignore SSL errors
                handler?.cancel()
                Toast.makeText(applicationContext, "SSL Error occurred", Toast.LENGTH_SHORT).show()
            }
        }
        
        val uri: Uri? = intent.data
        uri?.let {
            try {
                var data: String? = null
                
                if (uri.path.equals("/web")) {
                    val urlParam = intent.data?.getQueryParameter("url")
                    if (urlParam != null && isUrlValid(urlParam)) {
                        data = urlParam
                    }
                } else if (uri.path.equals("/webview")) {
                    val urlParam = intent.data?.getQueryParameter("url")
                    if (urlParam != null && isUrlValid(urlParam)) {
                        data = urlParam
                    }
                }

                if (data == null) {
                    // Fail safely to a default secure page
                    data = "https://www.insecureshopapp.com"
                    // Alternatively, show error and close the activity
                    // Toast.makeText(this, "Invalid URL requested", Toast.LENGTH_SHORT).show()
                    // finish()
                    // return@let
                }
                
                webview.loadUrl(data)
                Prefs.getInstance(this).data = data
                
            } catch (e: Exception) {
                Log.e("WebViewActivity", "Error processing URL: ${e.message}")
                // Handle the exception securely
                Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Function to validate URLs against allowed domains
    private fun isUrlValid(urlString: String): Boolean {
        try {
            val url = Uri.parse(urlString)
            
            // Only allow https scheme for security
            if (url.scheme?.lowercase() != "https") {
                return false
            }
            
            val host = url.host?.lowercase() ?: return false
            
            // Check if the host exactly matches an allowed domain or is a valid subdomain
            return allowedDomains.any { domain ->
                host == domain || // Exact match
                (host.endsWith(".$domain") && // Valid subdomain pattern
                 host.length > domain.length + 1) // Prevent "xinsecureshopapp.com" style attacks
            }
        } catch (e: Exception) {
            Log.e("WebViewActivity", "URL validation error: ${e.message}")
            return false
        }
    }
}