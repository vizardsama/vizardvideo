package my.vizard.spendtrace.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import my.vizard.spendtrace.data.AppDatabase
import my.vizard.spendtrace.data.Transaction
import my.vizard.spendtrace.parser.Categorizer
import my.vizard.spendtrace.parser.Direction
import my.vizard.spendtrace.parser.NotificationParser

class PaymentNotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SourceApps.packages) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()

        val parsed = NotificationParser.parse(title, text) ?: return
        val raw = listOfNotNull(title, text).joinToString(" | ")
        val isDebit = parsed.direction == Direction.DEBIT
        val minuteBucket = sbn.postTime / 60_000

        val tx = Transaction(
            amountSen = parsed.amountSen,
            isDebit = isDebit,
            merchant = parsed.merchant,
            category = if (isDebit) Categorizer.categorize(parsed.merchant, raw) else "Pendapatan",
            sourcePackage = sbn.packageName,
            rawText = raw,
            timestamp = sbn.postTime,
            fingerprint = "${sbn.packageName}|${parsed.amountSen}|$isDebit|$minuteBucket",
        )
        val dao = AppDatabase.get(applicationContext).transactions()
        scope.launch { dao.insert(tx) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
