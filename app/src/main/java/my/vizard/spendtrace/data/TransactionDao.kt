package my.vizard.spendtrace.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tx: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun all(): Flow<List<Transaction>>

    @Query("SELECT category, SUM(amountSen) AS totalSen FROM transactions WHERE isDebit = 1 AND timestamp >= :from AND timestamp < :to GROUP BY category ORDER BY totalSen DESC")
    fun spendByCategory(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("UPDATE transactions SET category = :category WHERE id = :id")
    suspend fun setCategory(id: Long, category: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}
