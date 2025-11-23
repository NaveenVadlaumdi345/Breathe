package uk.ac.tees.mad.breathe

import android.Manifest
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.breathe.data.model.Quote
import uk.ac.tees.mad.breathe.data.model.Session
import uk.ac.tees.mad.breathe.data.model.User
import uk.ac.tees.mad.breathe.repository.HomeRepository
import uk.ac.tees.mad.breathe.repository.SessionRepository
import uk.ac.tees.mad.breathe.repository.UserPreferences

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
    val noiseLevel: Float = 0f
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val sessionRepository: SessionRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
            val quoteResult = repository.fetchRandomQuote()
            Log.d("MainViewModel", "quoteResult: $quoteResult")
            val quote = quoteResult.getOrNull()

            val prefsResult = repository.getPreferences()
            val prefs = prefsResult.getOrNull() ?: UserPreferences()

            _ui.update {
                it.copy(isLoading = false, quote = quote, prefs = prefs, error = null)

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
    private var startTime: Long = 0L
    private var totalSeconds: Int = 0

    private val _state = androidx.compose.runtime.mutableStateOf(SessionUiState())
    val state: androidx.compose.runtime.State<SessionUiState> = _state

    fun startSession(duration: Int, vibrator: Vibrator) {
        durationMinutes = duration
        totalSeconds = duration * 60
        startTime = System.currentTimeMillis()
        _state.value = SessionUiState(isRunning = true, elapsedSeconds = 0)

        viewModelScope.launch {
            while (_state.value.isRunning && _state.value.elapsedSeconds < totalSeconds) {
                // Simulate inhale/exhale 4s each
                vibratePattern(vibrator, inhale = true)
                delay(4000)
                vibratePattern(vibrator, inhale = false)
                delay(4000)
                _state.value = _state.value.copy(elapsedSeconds = _state.value.elapsedSeconds + 8)
            }

            // Save on completion
            if (_state.value.elapsedSeconds >= totalSeconds) {
                sessionRepository.saveSession(
                    Session(
                        duration = duration,
                        timestamp = System.currentTimeMillis(),
                        averageNoiseLevel = _state.value.noiseLevel,
                        completed = true
                    )
                )
                _state.value = _state.value.copy(isRunning = false)
            }
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibratePattern(vibrator: Vibrator, inhale: Boolean) {
        val duration = if (inhale) 400L else 400L
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun pauseSession() {
        _state.value = _state.value.copy(isPaused = !_state.value.isPaused)
    }

    fun stopSession() {
        _state.value = _state.value.copy(isRunning = false)
    }
}
