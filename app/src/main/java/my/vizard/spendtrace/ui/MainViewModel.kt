package my.vizard.spendtrace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.vizard.spendtrace.data.AppDatabase
import java.util.Calendar

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).transactions()

    val transactions = dao.all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthSummary = run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val from = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        dao.spendByCategory(from, cal.timeInMillis)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setCategory(id: Long, category: String) = viewModelScope.launch { dao.setCategory(id, category) }
    fun delete(id: Long) = viewModelScope.launch { dao.delete(id) }
}
