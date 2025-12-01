package uk.ac.tees.mad.breathe.ui.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import uk.ac.tees.mad.breathe.MainViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    duration: Int,
    viewModel: MainViewModel = hiltViewModel(), // get injected VM if using Hilt
    onEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    val vibrator = remember {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    }

    val state by viewModel.state.collectAsState()

    // Request start only once when screen shows: viewModel will perform countdown -> start
    LaunchedEffect(duration) {
        viewModel.prepareAndStartSession(duration)
    }

    // Progress: when counting down, show 0; otherwise elapsed/total
    val progress = when {
        state.isCountingDown -> 0f
        duration <= 0 -> 0f
        else -> state.elapsedSeconds.toFloat() / (duration * 60f)
    }.coerceIn(0f, 1f)

    // Decorative breathing pulse animation
    val scale = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Pulse circle
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // If counting down, show start timestamp + countdown
            if (state.isCountingDown) {
                val startInstant = state.scheduledStartMillis?.let { Instant.ofEpochMilli(it) }
                val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm:ss", Locale.getDefault())
                Text(
                    text = startInstant?.atZone(ZoneId.systemDefault())?.format(fmt)
                        ?.let { "Starting at: $it" } ?: "Preparing...",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = (state.countdownSeconds ?: 3).toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                )
            } else {
                // Running or idle UI
                Text(
                    text = "Breathe",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val elapsed = state.elapsedSeconds
                val minutes = (elapsed / 60)
                val seconds = (elapsed % 60)
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth(0.6f))

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                // Pause / Resume button
                Button(onClick = { viewModel.togglePauseResume() }) {
                    Text(if (state.isPaused) "Resume" else "Pause")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // End button
                Button(
                    onClick = {
                        viewModel.stopSession(save = true)
                        onEnd()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("End")
                }
            }
        }
    }
}
