package uk.ac.tees.mad.breathe

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import uk.ac.tees.mad.breathe.data.model.Quote
import uk.ac.tees.mad.breathe.data.model.Session
import uk.ac.tees.mad.breathe.data.model.User
import uk.ac.tees.mad.breathe.repository.HomeRepository
import uk.ac.tees.mad.breathe.repository.SessionRepository
import uk.ac.tees.mad.breathe.repository.UserPreferences
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
    val isRunning: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val sessionRepository: SessionRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase  // Changed from Firestore to Realtime DB
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn
    private val _ui = MutableStateFlow(HomeUiState(isLoading = true))
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        refreshAll()
        auth.addAuthStateListener { firebaseAuth ->
            _isLoggedIn.value = firebaseAuth.currentUser != null
        }
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
                // Changed to Realtime DB for consistency with requirements
                db.getReference("users").child(uid).setValue(newUser)
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
                withTimeout(10000L) { // 10-second timeout
                    val quoteResult = repository.fetchRandomQuote()
                    val prefsResult = repository.getPreferences()

                    val quote = quoteResult.getOrNull() ?: Quote("Take a deep breath.", "Unknown")
                    val prefs = prefsResult.getOrNull() ?: UserPreferences()

                    _ui.update {
                        it.copy(
                            isLoading = false,
                            quote = quote,
                            prefs = prefs,
                            error = null
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = "Request timed out. Please try again."
                    )
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load home data"
                    )
                }
                Log.e("MainViewModel", "Error in refreshAll: ${e.message}", e)
            }
        }
    }


    fun toggleAmbient(enabled: Boolean) {
        val newPrefs = _ui.value.prefs.copy(ambientNoiseDetection = enabled)
        _ui.update { it.copy(prefs = newPrefs) }
        viewModelScope.launch {
            val res = repository.savePreferences(newPrefs)
            if (res.isFailure) {
                _ui.update { it.copy(error = res.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    fun setDefaultDuration(minutes: Int) {
        val newPrefs = _ui.value.prefs.copy(defaultDurationMinutes = minutes)
        _ui.update { it.copy(prefs = newPrefs) }
        viewModelScope.launch {
            val res = repository.savePreferences(newPrefs)
            if (res.isFailure) {
                _ui.update { it.copy(error = res.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    private var durationMinutes: Int = 1
    private var totalSeconds: Int = 0

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    fun startSession(duration: Int, vibrator: Vibrator) {
        durationMinutes = duration
        totalSeconds = duration * 60
        _state.update { it.copy(isRunning = true, isPaused = false, elapsedSeconds = 0) }

        // Breathing guidance loop
        viewModelScope.launch {
            while (_state.value.isRunning && _state.value.elapsedSeconds < totalSeconds) {
                if (_state.value.isPaused) {
                    delay(100)
                    continue
                }
                vibratePattern(vibrator, inhale = true)
                delay(4000) // Inhale during vibration
                delay(4000) // Hold
                vibratePattern(vibrator, inhale = false)
                delay(4000) // Exhale during vibration
                delay(4000) // Hold
            }
            finalizeSession()
        }

        // Timer update loop
        viewModelScope.launch {
            while (_state.value.isRunning && _state.value.elapsedSeconds < totalSeconds) {
                delay(1000)
                if (!_state.value.isPaused) {
                    _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibratePattern(vibrator: Vibrator, inhale: Boolean) {
        // Use continuous vibration for both inhale and exhale for consistency in box breathing
        vibrator.vibrate(VibrationEffect.createOneShot(4000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun pauseSession() {
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    fun stopSession() {
        _state.update { it.copy(isRunning = false) }
    }

    private suspend fun finalizeSession() {
        _state.update { it.copy(isRunning = false) }
        if (_state.value.elapsedSeconds >= totalSeconds) {
            sessionRepository.saveSession(
                Session(
                    duration = durationMinutes,
                    timestamp = System.currentTimeMillis(),
                    averageNoiseLevel = 0f,
                    completed = true
                )
            )
        }
    }
}