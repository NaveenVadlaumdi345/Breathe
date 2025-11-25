package uk.ac.tees.mad.breathe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.breathe.data.local.SessionDao
import uk.ac.tees.mad.breathe.data.model.Session
import java.text.SimpleDateFormat
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
            sessionDao.getAll().forEach {
                // For now we just delete all (optional: create deleteAll DAO)
                sessionDao.insert(it.copy(completed = false))
            }
            _state.value = _state.value.copy(sessions = emptyList())
        }
    }

    private fun calculateWeeklyStreak(sessions: List<Session>): Int {
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        return sessions.count {
            val sessionWeek = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(Calendar.WEEK_OF_YEAR)
            sessionWeek == currentWeek
        }
    }

    private fun calculateMonthlyStreak(sessions: List<Session>): Int {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        return sessions.count {
            val sessionMonth = Calendar.getInstance().apply {
                timeInMillis = it.timestamp
            }.get(Calendar.MONTH)
            sessionMonth == currentMonth
        }
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }
}
