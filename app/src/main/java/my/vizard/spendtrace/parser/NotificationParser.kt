package my.vizard.spendtrace.parser

enum class Direction { DEBIT, CREDIT }

data class ParsedTransaction(
    val amountSen: Long,
    val direction: Direction,
    val merchant: String?,
)

/** Pure Kotlin, no Android deps, so it can be unit-tested on the JVM. */
object NotificationParser {

    private val amountRegex = Regex("""(?:RM|MYR)\s?([0-9]{1,3}(?:,[0-9]{3})*|[0-9]+)(?:\.([0-9]{1,2}))?""", RegexOption.IGNORE_CASE)

    private val debitWords = listOf(
        "paid", "payment", "purchase", "spent", "debited", "transfer to", "transferred to",
        "sent", "bayaran", "dibayar", "pembelian", "dibelanjakan", "didebitkan", "pindahan ke",
        "withdraw", "pengeluaran",
    )
    private val creditWords = listOf(
        "received", "credited", "refund", "cashback", "diterima", "dikreditkan", "bayaran balik",
        "transfer from",
    )
    // Notifications that carry an amount but are not a completed payment.
    private val ignoreWords = listOf(
        "otp", "tac", "one-time", "promo", "voucher", "win", "save up to", "claim", "reminder",
        "due on", "minimum payment", "statement", "baki", "balance is", "your balance",
    )

    private fun wordRegex(words: List<String>) =
        Regex("""\b(?:${words.joinToString("|") { Regex.escape(it) }})\b""", RegexOption.IGNORE_CASE)

    private val debitRegex = wordRegex(debitWords)
    private val creditRegex = wordRegex(creditWords)
    private val ignoreRegex = wordRegex(ignoreWords.map { it.trim() })

    private val merchantPatterns = listOf(
        Regex("""\b(?:paid to|payment to|transfer(?:red)? to|sent to|kepada|ke)\s+(.+?)(?:\s+(?:on|at|via|pada|dengan|ref|using)\b|[.,;]|\s+RM|\s+MYR|$)""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:purchase|spent|transaction|transaksi|pembelian)\s+(?:at|di|@)\s+(.+?)(?:\s+(?:on|pada|via|using|with)\b|[.,;]|\s+RM|\s+MYR|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:RM|MYR)\s?[0-9][0-9,]*(?:\.[0-9]{1,2})?\s+to\s+(.+?)(?:\s+(?:on|at|via|pada|dengan|ref|using)\b|[.,;]|$)""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:at|di)\s+([A-Za-z0-9][A-Za-z0-9 &'*\-]{2,40}?)(?:\s+(?:on|pada|via|using|with)\b|[.,;]|$)""", RegexOption.IGNORE_CASE),
    )

    fun parse(title: String?, text: String?): ParsedTransaction? {
        val body = listOfNotNull(title, text).joinToString(" ").replace(Regex("\\s+"), " ").trim()
        if (body.isEmpty()) return null

        val amountMatch = amountRegex.find(body) ?: return null
        if (ignoreRegex.containsMatchIn(body)) return null

        val ringgit = amountMatch.groupValues[1].replace(",", "").toLong()
        val sen = amountMatch.groupValues[2].let {
            when (it.length) { 0 -> 0L; 1 -> it.toLong() * 10; else -> it.toLong() }
        }
        val total = ringgit * 100 + sen
        if (total <= 0) return null

        val direction = when {
            debitRegex.containsMatchIn(body) -> Direction.DEBIT
            creditRegex.containsMatchIn(body) -> Direction.CREDIT
            else -> return null
        }

        val merchant = merchantPatterns.firstNotNullOfOrNull { it.find(body)?.groupValues?.get(1)?.trim() }
            ?.trimEnd('.', ',', ' ')
            ?.takeIf { it.isNotBlank() && !amountRegex.containsMatchIn(it) }

        return ParsedTransaction(total, direction, merchant)
    }
}
