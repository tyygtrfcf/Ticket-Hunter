package com.example.tickethunter

import android.os.Bundle
import android.os.CountDownTimer
import java.text.SimpleDateFormat
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.widget.*
import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var urlInput: EditText
    private lateinit var zoneInput: EditText

    private var isRunning = false
    private var countDownTimer: 
    CountDownTimer? = null
    private var saleTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 60)
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

        val saleDateInput = EditText(this).apply {
            hint = "Sale Date (DD/MM/YYYY)"
            setSingleLine(true)
        }

        val saleTimeInput = EditText(this).apply {
            hint = "Sale Time (HH:MM, SGT)"
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
            setPadding(0, 12, 0, 12)
        }

        startButton = Button(this).apply {
            text = "START"
        }

        stopButton = Button(this).apply {
            text = "STOP"
            isEnabled = false
        }

        CookieManager.getInstance().setAcceptCookie(true)

        webView = WebView(this).apply {

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url = request?.url?.toString() ?: return false

                    if (url.startsWith("intent://")) {
                        val httpsUrl = url
                            .replaceFirst("intent://", "https://")
                            .substringBefore("#Intent;")

                        view?.loadUrl(httpsUrl)
                        return true
                    }

                    return false
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    statusText.text = "Status: Eventpop Loaded"
                    CookieManager.getInstance().flush()
                }
            }

            webChromeClient = WebChromeClient()
        }

        CookieManager.getInstance().flush()

        // Initial interface

        root.addView(title)
        root.addView(urlInput)
        root.addView(saleDateInput)
        root.addView(saleTimeInput)
        root.addView(zoneInput)
        root.addView(statusText)
        root.addView(startButton)
        root.addView(stopButton)

        setContentView(root)

        // START BUTTON

        startButton.setOnClickListener {

            val url = urlInput.text.toString().trim()

            if (!url.startsWith("https://")) {
                statusText.text = "Please enter a valid HTTPS URL"
                return@setOnClickListener
            }


            val dateText = saleDateInput.text.toString().trim()
val timeText = saleTimeInput.text.toString().trim()

try {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    dateFormat.isLenient = false

    val saleDate = dateFormat.parse("$dateText $timeText")

    if (saleDate == null) {
        statusText.text = "Invalid sale date or time"
        return@setOnClickListener
    }

    saleTimeMillis = saleDate.time

    if (saleTimeMillis <= System.currentTimeMillis()) {
    statusText.text = "Sale time must be in the future"
    return@setOnClickListener
    }

    startSaleCountdown()

    isRunning = true

} catch (e: Exception) {
    statusText.text = "Please enter date and time correctly"
    return@setOnClickListener
}

            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )

            startButton.isEnabled = false
            stopButton.isEnabled = true

            statusText.text = "Opening Eventpop... Countdown running"

            root.removeView(title)
            root.removeView(urlInput)
            root.removeView(saleDateInput)
            root.removeView(saleTimeInput)
            root.removeView(zoneInput)
            root.removeView(startButton)

            root.addView(
                webView,
                0,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )

            webView.loadUrl(url)
        }

        // STOP BUTTON

        stopButton.setOnClickListener {

            isRunning = false
            countDownTimer?.cancel()
            
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )

            webView.stopLoading()

            root.removeView(webView)

            root.addView(title, 0)
            root.addView(urlInput, 1)
            root.addView(saleDateInput, 2)
            root.addView(saleTimeInput, 3)
            root.addView(zoneInput, 4)
            root.addView(startButton, 5)

            statusText.text = "Status: Stopped"

            startButton.isEnabled = true
            stopButton.isEnabled = false
        }
    }
private fun startSaleCountdown() {

        countDownTimer?.cancel()

        val remainingTime = saleTimeMillis - System.currentTimeMillis()

        if (remainingTime <= 0) {
            statusText.text = "Tickets are on sale now!"
            return
        }

        countDownTimer = object : CountDownTimer(remainingTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                statusText.text =
                    "Sale starts in: %02d days %02d:%02d:%02d".format(
                        days, hours, minutes, seconds
                    )
            }

            override fun onFinish() {
                statusText.text = "Tickets are on sale now!"
            }
        }.start()
}
    override fun onDestroy() {
        countDownTimer?.cancel()
        
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }

        super.onDestroy()
    }
}
