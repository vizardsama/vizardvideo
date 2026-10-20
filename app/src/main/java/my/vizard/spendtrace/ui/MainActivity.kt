package my.vizard.spendtrace.ui

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import my.vizard.spendtrace.data.Transaction
import my.vizard.spendtrace.parser.Categorizer
import my.vizard.spendtrace.service.PaymentNotificationService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()
    private var listenerEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    if (!listenerEnabled) PermissionScreen() else HomeScreen(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        listenerEnabled = isListenerEnabled()
    }

    private fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        val me = ComponentName(this, PaymentNotificationService::class.java)
        return flat.split(":").mapNotNull { ComponentName.unflattenFromString(it) }.any { it == me }
    }

    @Composable
    private fun PermissionScreen() {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("SpendTrace", style = MaterialTheme.typography.headlineMedium)
            Text(
                "App ni perlu akses notification untuk detect bayaran dari app bank dan e-wallet. " +
                    "Semua data disimpan dalam phone sahaja, tiada dihantar ke mana-mana."
            )
            Button(onClick = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                Text("Buka tetapan Notification access")
            }
        }
    }
}

private fun rm(sen: Long) = "RM%,.2f".format(Locale.US, sen / 100.0)

@Composable
private fun HomeScreen(vm: MainViewModel) {
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val summary by vm.monthSummary.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Transaction?>(null) }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Bulan ini", style = MaterialTheme.typography.titleLarge)
            Text("Jumlah belanja: " + rm(summary.sumOf { it.totalSen }), style = MaterialTheme.typography.titleMedium)
        }
        items(summary) { c ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(c.category); Text(rm(c.totalSen))
            }
        }
        item { Text("Transaksi", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp)) }
        if (txs.isEmpty()) item { Text("Belum ada transaksi. Cuba buat satu bayaran.") }
        items(txs, key = { it.id }) { t ->
            Card(Modifier.fillMaxWidth().clickable { editing = t }) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t.merchant ?: "(tak dikenali)", style = MaterialTheme.typography.titleMedium)
                        Text((if (t.isDebit) "-" else "+") + rm(t.amountSen))
                    }
                    Text("${t.category} · ${fmt.format(Date(t.timestamp))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    editing?.let { t ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Tukar kategori") },
            text = {
                Column {
                    Categorizer.categories.forEach { c ->
                        TextButton(onClick = { vm.setCategory(t.id, c); editing = null }) { Text(c) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.delete(t.id); editing = null }) { Text("Padam transaksi") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Tutup") } },
        )
    }
}
