package com.example.kotlindormify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class PaymentFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.payment_fragment, container, false)

        val webView: WebView = rootView.findViewById(R.id.webview)
        // Enable JavaScript (if needed)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Attach a custom WebViewClient
        webView.webViewClient = MyWebViewClient()

        // Load a URL (e.g., "https://gcash.com")
        webView.loadUrl("https://hitpay.shop/s/kbtjjt")

        return rootView
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            // The web page has finished loading
        }
    }
}
