package my.vizard.spendtrace.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions", indices = [Index(value = ["fingerprint"], unique = true)])
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountSen: Long,
    val isDebit: Boolean,
    val merchant: String?,
    val category: String,
    val sourcePackage: String,
    val rawText: String,
    val timestamp: Long,
    /** package + amount + direction + minute bucket; stops the same alert (posted twice) being stored twice. */
    val fingerprint: String,
)

data class CategoryTotal(val category: String, val totalSen: Long)
