package uk.ac.tees.mad.breathe.ui.presentation

import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.breathe.MainViewModel

@Composable
fun SessionScreen(duration: Int, viewModel: MainViewModel, onEnd: () -> Unit = {}) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startSession(duration, vibrator)
    }

    val progress = state.elapsedSeconds.toFloat() / (duration * 60f)
    val scale = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
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
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Breathe",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.6f))

            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Button(onClick = { viewModel.pauseSession() }) {
                    Text(if (state.isPaused) "Resume" else "Pause")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    viewModel.stopSession()
                    onEnd()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("End")
                }
            }
        }
    }
}