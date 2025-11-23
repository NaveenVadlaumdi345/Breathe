package uk.ac.tees.mad.breathe.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import uk.ac.tees.mad.breathe.R
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.breathe.MainViewModel

@Composable
fun HomeScreen(navController: NavController, vm: MainViewModel) {
    val state by vm.ui.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
        ) {
            // Header + Quote card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Good day", style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.quote?.text ?: "Take a moment - inhale, exhale.",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "- ${state.quote?.author ?: "Breathe"}",
                        style = MaterialTheme.typography.bodySmall)
                }

                // refresh icon
                IconButton(onClick = { vm.refreshAll() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Preferences card
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Ambient noise detection", modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.prefs.ambientNoiseDetection,
                            onCheckedChange = { enabled -> vm.toggleAmbient(enabled) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Default duration", modifier = Modifier.weight(1f))
                        DurationPicker(selected = state.prefs.defaultDurationMinutes, onSelect = { minutes ->
                            vm.setDefaultDuration(minutes)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Exercises", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(8.dp))

            // Buttons for durations
            val durations = listOf(1, 3, 5, 10)
            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                durations.forEach { d ->
                    ExerciseCard(duration = d, onClick = {
                        navController.navigate("session/$d")
                    })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loading / error
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ExerciseCard(duration: Int, onClick: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(150.dp)
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${duration} min", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Start", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun DurationPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row {
        val options = listOf(1, 3, 5, 10)
        options.forEach { minutes ->
            val selectedBg = if (minutes == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val selectedText = if (minutes == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(width = 56.dp, height = 32.dp)
                    .background(selectedBg, RoundedCornerShape(8.dp))
                    .clickable { onSelect(minutes) },
                contentAlignment = Alignment.Center
            ) {
                Text("$minutes", color = selectedText)
            }
        }
    }
}
