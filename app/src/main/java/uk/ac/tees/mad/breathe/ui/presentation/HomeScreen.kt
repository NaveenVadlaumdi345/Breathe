package uk.ac.tees.mad.breathe.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import uk.ac.tees.mad.breathe.MainViewModel
import uk.ac.tees.mad.breathe.R
import uk.ac.tees.mad.breathe.ui.navigation.MainnavItems

@Composable
fun HomeScreen(navController: NavController, vm: MainViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading && state.quote == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Good day 🌿",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.quote?.text ?: "Take a deep breath and relax.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "- ${state.quote?.author ?: "Breathe"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { vm.refreshAll() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh quote")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Default Duration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        DurationPicker(
                            selected = state.prefs.defaultDurationMinutes,
                            onSelect = { minutes -> vm.setDefaultDuration(minutes) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    navController.navigate(MainnavItems.Session.x.replace("{duration}", state.prefs.defaultDurationMinutes.toString()))
                }, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Start")
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Exercises",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val durations = listOf(1, 3, 5, 10)
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                ) {
                    durations.forEach { d ->
                        ExerciseCard(duration = d) {
                            navController.navigate(
                                MainnavItems.Session.x.replace("{duration}", d.toString())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${duration} min",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val options = listOf(1, 3, 5, 10)
        options.forEach { minutes ->
            val isSelected = minutes == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(minutes) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$minutes min",
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}