package com.example.tickethunter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private const val PREFS_NAME = "ticket_hunter_preferences"
private val SALE_TIME_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicketHunterScreen()
                }
            }
        }
    }
}

@Composable
private fun TicketHunterScreen() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var eventName by remember {
        mutableStateOf(prefs.getString("event_name", "").orEmpty())
    }
    var saleDateTime by remember {
        mutableStateOf(prefs.getString("sale_date_time", "").orEmpty())
    }
    var ticketUrl by remember {
        mutableStateOf(prefs.getString("ticket_url", "").orEmpty())
    }
    var seatPreferences by remember {
        mutableStateOf(prefs.getString("seat_preferences", "").orEmpty())
    }
    var maximumPrice by remember {
        mutableStateOf(prefs.getString("maximum_price", "").orEmpty())
    }

    var signedIn by remember {
        mutableStateOf(prefs.getBoolean("check_signed_in", false))
    }
    var paymentReady by remember {
        mutableStateOf(prefs.getBoolean("check_payment_ready", false))
    }
    var eventDetailsChecked by remember {
        mutableStateOf(prefs.getBoolean("check_event_details", false))
    }

    var remainingSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(saleDateTime) {
        while (true) {
            val targetMillis = parseSaleTimeMillis(saleDateTime)
            remainingSeconds = if (targetMillis == null) {
                0L
            } else {
                val millisecondsLeft = targetMillis - System.currentTimeMillis()
                if (millisecondsLeft <= 0L) {
                    0L
                } else {
                    (millisecondsLeft + 999L) / 1000L
                }
            }
            delay(1_000L)
        }
    }

    val parsedSaleTime = parseSaleTimeMillis(saleDateTime)
    val countdownLabel = when {
        saleDateTime.isBlank() -> "Add a sale date and time to start the countdown."
        parsedSaleTime == null -> "Enter the date and time in the format shown below."
        remainingSeconds <= 0L -> "The sale time has passed."
        else -> formatCountdown(remainingSeconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ticket Hunter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Get organized before the sale. Seat selection and checkout happen on the official ticket site.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Countdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = countdownLabel,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Uses your phone's local time.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Event setup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = eventName,
            onValueChange = {
                eventName = it
                prefs.edit().putString("event_name", it).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Event name") },
            placeholder = { Text("Example: Summer concert") },
            singleLine = true
        )

        OutlinedTextField(
            value = saleDateTime,
            onValueChange = {
                saleDateTime = it
                prefs.edit().putString("sale_date_time", it).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Sale date and time") },
            placeholder = { Text("2026-12-01 10:00") },
            supportingText = { Text("Format: yyyy-MM-dd HH:mm, in your phone's local time") },
            singleLine = true
        )

        OutlinedTextField(
            value = ticketUrl,
            onValueChange = {
                ticketUrl = it
                prefs.edit().putString("ticket_url", it).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Official ticket page URL") },
            placeholder = { Text("https://example.com/tickets") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        Text(
            text = "Seat and price preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = seatPreferences,
            onValueChange = {
                seatPreferences = it
                prefs.edit().putString("seat_preferences", it).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Preferred sections or notes") },
            placeholder = { Text("Example: lower bowl, near center") },
            minLines = 2,
            maxLines = 4
        )

        OutlinedTextField(
            value = maximumPrice,
            onValueChange = {
                maximumPrice = it
                prefs.edit().putString("maximum_price", it).apply()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Maximum ticket price") },
            placeholder = { Text("Example: 150.00") },
            supportingText = { Text("Enter an amount in your preferred currency.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Text(
            text = "Preparation checklist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ChecklistItem(
                    label = "I can sign in to the official ticket site",
                    checked = signedIn,
                    onCheckedChange = {
                        signedIn = it
                        prefs.edit().putBoolean("check_signed_in", it).apply()
                    }
                )
                ChecklistItem(
                    label = "My payment method is ready",
                    checked = paymentReady,
                    onCheckedChange = {
                        paymentReady = it
                        prefs.edit().putBoolean("check_payment_ready", it).apply()
                    }
                )
                ChecklistItem(
                    label = "I checked the event date and ticket details",
                    checked = eventDetailsChecked,
                    onCheckedChange = {
                        eventDetailsChecked = it
                        prefs.edit().putBoolean("check_event_details", it).apply()
                    }
                )
            }
        }

        Button(
            onClick = { openTicketPage(context, ticketUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = isValidWebUrl(ticketUrl)
        ) {
            Text("Open official ticket page")
        }

        Text(
            text = "Ticket Hunter does not select seats, tap for you, bypass queues, or make purchases.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ChecklistItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun parseSaleTimeMillis(value: String): Long? {
    if (value.isBlank()) return null

    return try {
        LocalDateTime.parse(value.trim(), SALE_TIME_FORMAT)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    } catch (_: Exception) {
        null
    }
}

private fun formatCountdown(totalSeconds: Long): String {
    val days = totalSeconds / 86_400L
    val hours = (totalSeconds % 86_400L) / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return if (days > 0L) {
        "%d days %02d:%02d:%02d".format(days, hours, minutes, seconds)
    } else {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

private fun isValidWebUrl(value: String): Boolean {
    val uri = Uri.parse(value.trim())
    return uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank()
}

private fun openTicketPage(context: Context, value: String) {
    if (!isValidWebUrl(value)) {
        Toast.makeText(context, "Enter a valid official ticket page URL.", Toast.LENGTH_SHORT)
            .show()
        return
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value.trim()))
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No browser is available to open this page.", Toast.LENGTH_SHORT)
            .show()
    }
}
