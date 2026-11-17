package com.example.tickethunter

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.*
import android.view.View
import android.graphics.Color
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var zoneInput: EditText
    private lateinit var statusText: TextView

    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "TICKET HUNTER"
            textSize = 24f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 20)
        }

        urlInput = EditText(this).apply {
            hint = "Paste Eventpop event URL"
            setSingleLine(true)
        }

        zoneInput = EditText(this).apply {
            hint = "Preferred Zones: A2,A1,B1"
            setSingleLine(true)
            setText("A2,A1,B1")
        }

        val startButton = Button(this).apply {
            text = "START"
        }

        val stopButton = Button(this).apply {
            text = "STOP"
            isEnabled = false
        }

        statusText = TextView(this).apply {
            text = "Status: Ready"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 12, 0, 12)
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }

        layout.addView(title)
        layout.addView(urlInput)
        layout.addView(zoneInput)
        layout.addView(startButton)
        layout.addView(stopButton)
        layout.addView(statusText)

        setContentView(layout)

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

            webView.loadUrl(url)
            setContentView(
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(statusText)
                    addView(webView,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                    )
                    addView(stopButton)
                }
            )
        }

        stopButton.setOnClickListener {
            isRunning = false
            webView.stopLoading()

            statusText.text = "Status: Stopped"
            stopButton.isEnabled = false
            startButton.isEnabled = true
        }
    }
}
