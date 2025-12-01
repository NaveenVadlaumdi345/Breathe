package uk.ac.tees.mad.breathe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.breathe.data.local.SessionDao
import uk.ac.tees.mad.breathe.data.model.Session
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<Session> = emptyList(),
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val sessionDao: SessionDao
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState(isLoading = true))
    val state: StateFlow<HistoryUiState> = _state

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val sessions = sessionDao.getAll()
            _state.value = _state.value.copy(
                sessions = sessions,
                weeklyCount = calculateWeeklyStreak(sessions),
                monthlyCount = calculateMonthlyStreak(sessions),
                isLoading = false
            )
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            sessionDao.clearAll()
            _state.value = _state.value.copy(sessions = emptyList(), weeklyCount = 0, monthlyCount = 0)
        }
    }

    private fun calculateWeeklyStreak(sessions: List<Session>): Int {
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)
        return sessions.count {
            val sessionCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            sessionCal.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                    sessionCal.get(Calendar.YEAR) == currentYear
        }
    }

    private fun calculateMonthlyStreak(sessions: List<Session>): Int {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        return sessions.count {
            val sessionCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            sessionCal.get(Calendar.MONTH) == currentMonth &&
                    sessionCal.get(Calendar.YEAR) == currentYear
        }
    }

    fun formatDate(timeMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a", Locale.getDefault())
        val instant = Instant.ofEpochMilli(timeMillis)
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}
