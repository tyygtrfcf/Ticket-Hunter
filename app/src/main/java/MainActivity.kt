package com.example.tickethunter

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.*
import android.graphics.Color
import android.view.ViewGroup
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var urlInput: EditText
    private lateinit var zoneInput: EditText

    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "TICKET HUNTER"
            textSize = 24f
            setTextColor(Color.BLACK)
        }

        urlInput = EditText(this).apply {
            hint = "Eventpop event URL"
            setSingleLine(true)
        }

        zoneInput = EditText(this).apply {
            hint = "Preferred Zones"
            setText("A2,A1,B1")
            setSingleLine(true)
        }

        statusText = TextView(this).apply {
            text = "Status: Ready"
            textSize = 16f
            setTextColor(Color.DKGRAY)
        }

        startButton = Button(this).apply {
            text = "START"
        }

        stopButton = Button(this).apply {
            text = "STOP"
            isEnabled = false
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    statusText.text = "Eventpop page loaded"
                }
            }
            webChromeClient = WebChromeClient()
        }

        root.addView(title)
        root.addView(urlInput)
        root.addView(zoneInput)
        root.addView(statusText)
        root.addView(startButton)
        root.addView(stopButton)

        setContentView(root)

        startButton.setOnClickListener {
            val url = urlInput.text.toString().trim()

            if (!url.startsWith("https://")) {
                statusText.text = "Please enter a valid HTTPS URL"
                return@setOnClickListener
            }

            isRunning = true
            startButton.isEnabled = false
            stopButton.isEnabled = true

            statusText.text = "Opening Eventpop..."

            root.removeView(title)
            root.removeView(urlInput)
            root.removeView(zoneInput)
            root.removeView(startButton)

            root.addView(
                webView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )

            webView.loadUrl(url)
        }

        stopButton.setOnClickListener {
            isRunning = false
            webView.stopLoading()

            root.removeView(webView)
            root.addView(title, 0)
            root.addView(urlInput, 1)
            root.addView(zoneInput, 2)
            root.addView(startButton, 4)

            statusText.text = "Status: Stopped"
            startButton.isEnabled = true
            stopButton.isEnabled = false
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
