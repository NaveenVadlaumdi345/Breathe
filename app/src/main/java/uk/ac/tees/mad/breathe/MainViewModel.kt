package uk.ac.tees.mad.breathe

import android.Manifest
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uk.ac.tees.mad.breathe.data.model.Quote
import uk.ac.tees.mad.breathe.data.model.Session
import uk.ac.tees.mad.breathe.data.model.User
import uk.ac.tees.mad.breathe.repository.HomeRepository
import uk.ac.tees.mad.breathe.repository.SessionRepository
import uk.ac.tees.mad.breathe.repository.UserPreferences
import java.time.Instant
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val quote: Quote? = null,
    val prefs: UserPreferences = UserPreferences(),
    val error: String? = null
)

data class SessionUiState(
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isRunning: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownSeconds: Int? = null,
    val scheduledStartMillis: Long? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val sessionRepository: SessionRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    private val _ui = MutableStateFlow(HomeUiState(isLoading = true))
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    private var requestedMinutes: Int = 1
    private var totalSecondsTarget: Int = 0

    init {
        refreshAll()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                _authState.value = AuthState.Idle
                _isLoggedIn.value = false
                Log.d("MainViewModel", "User logged out -> AuthState.Idle")
            } else {
                _authState.value = AuthState.Success(user.uid)
                _isLoggedIn.value = true
                Log.d("MainViewModel", "User logged in -> AuthState.Success(${user.uid})")
            }
        }
    }

    fun logOut(){
        auth.signOut()
        _authState.value = AuthState.Idle
        _isLoggedIn.value = false
    }

    fun loginUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                _authState.value = AuthState.Success(uid)
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    fun signUpUser(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val newUser = User(uid = uid, email = email, name = name)
                firestore.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success(uid)
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error("Failed to store user data")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Signup failed")
            }
    }


    fun refreshAll() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                withTimeout(10_000L) {
                    val quote = repository.fetchRandomQuote().getOrNull()
                        ?: Quote("Take a deep breath.", "Unknown")
                    val prefs = repository.getPreferences().getOrNull() ?: UserPreferences()
                    _ui.update { it.copy(isLoading = false, quote = quote, prefs = prefs) }
                }
            } catch (e: TimeoutCancellationException) {
                _ui.update { it.copy(isLoading = false, error = "Request timed out. Please try again.") }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
                Log.e("MainViewModel", "refreshAll error", e)
            }
        }
    }

    fun toggleAmbient(enabled: Boolean) {
        val newPrefs = _ui.value.prefs.copy(ambientNoiseDetection = enabled)
        _ui.update { it.copy(prefs = newPrefs) }
        viewModelScope.launch { repository.savePreferences(newPrefs) }
    }

    fun setDefaultDuration(minutes: Int) {
        val newPrefs = _ui.value.prefs.copy(defaultDurationMinutes = minutes)
        _ui.update { it.copy(prefs = newPrefs) }
        viewModelScope.launch { repository.savePreferences(newPrefs) }
    }


    fun prepareAndStartSession(durationMinutes: Int, vibrator: Vibrator? = null) {
        viewModelScope.launch {
            try {
                requestedMinutes = durationMinutes.coerceAtLeast(1)
                totalSecondsTarget = requestedMinutes * 60

                val countdownLength = 3
                val scheduledStart = System.currentTimeMillis() + (countdownLength * 1000L)

                _state.update {
                    it.copy(
                        isCountingDown = true,
                        countdownSeconds = countdownLength,
                        scheduledStartMillis = scheduledStart,
                        isRunning = false,
                        isPaused = false,
                        elapsedSeconds = 0
                    )
                }

                for (i in countdownLength downTo 1) {
                    _state.update { it.copy(countdownSeconds = i) }
                    safeVibrate(vibrator, 80)
                    delay(1000)
                }

                _state.update {
                    it.copy(
                        isCountingDown = false,
                        countdownSeconds = null,
                        scheduledStartMillis = null,
                        isRunning = true
                    )
                }

                startBreathingLoop(vibrator)
                startTimerLoop()
            } catch (e: Exception) {
                Log.e("MainViewModel", "prepareAndStartSession error", e)
                _state.value = SessionUiState()
            }
        }
    }

    fun togglePauseResume() {
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    fun stopSession(save: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isRunning = false) }
            val elapsed = _state.value.elapsedSeconds
            if (save) saveSessionRecord(elapsed, completed = elapsed >= totalSecondsTarget)
            _state.value = SessionUiState()
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            try {
                while (_state.value.isRunning && _state.value.elapsedSeconds < totalSecondsTarget) {
                    delay(1000)
                    if (!_state.value.isPaused) {
                        _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                    }
                }
                if (_state.value.isRunning) {
                    _state.update { it.copy(isRunning = false) }
                    saveSessionRecord(_state.value.elapsedSeconds, completed = true)
                    _state.value = SessionUiState()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Timer loop error", e)
            }
        }
    }

    private fun startBreathingLoop(vibrator: Vibrator?) {
        viewModelScope.launch {
            try {
                val inhale = 4000L
                val hold = 4000L
                val exhale = 4000L

                while (_state.value.isRunning && _state.value.elapsedSeconds < totalSecondsTarget) {
                    if (_state.value.isPaused) {
                        delay(200)
                        continue
                    }

                    vibrateForDuration(vibrator, inhale)
                    delay(hold)
                    vibrateForDuration(vibrator, exhale)
                    delay(hold)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Breathing loop error", e)
            }
        }
    }

    private fun safeVibrate(vibrator: Vibrator?, durationMs: Long) {
        try {
            if (vibrator == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.w("MainViewModel", "Vibration failed: ${e.message}")
        }
    }

    private fun vibrateForDuration(vibrator: Vibrator?, duration: Long) = safeVibrate(vibrator, duration)

    private suspend fun saveSessionRecord(elapsedSeconds: Int, completed: Boolean) {
        try {
            val minutes = (elapsedSeconds + 59) / 60
            val session = Session(
                duration = minutes.coerceAtLeast(1),
                timestamp = System.currentTimeMillis(),
                averageNoiseLevel = 0f,
                completed = completed
            )
            sessionRepository.saveSession(session)
            Log.d("MainViewModel", "Session saved: $session")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to save session", e)
        }
    }
}
