package uk.ac.tees.mad.breathe.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.breathe.SessionHistoryViewModel
import uk.ac.tees.mad.breathe.data.model.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: SessionHistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Session History") },
                actions = {
                    IconButton(
                        onClick = { vm.loadSessions() },
                        content = {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh sessions")
                        }
                    )
                    IconButton(
                        onClick = { vm.clearAll() },
                        content = {
                            Icon(Icons.Rounded.Delete, contentDescription = "Clear all history")
                        }
                    )
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.sessions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sessions yet.\nStart breathing today 🌿",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StreakHeader(
                            weekly = state.weeklyCount,
                            monthly = state.monthlyCount
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    items(state.sessions) { session ->
                        HistoryCard(session = session, dateText = vm.formatDate(session.timestamp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakHeader(weekly: Int, monthly: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Weekly", style = MaterialTheme.typography.labelMedium)
            Text(
                "$weekly",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Monthly", style = MaterialTheme.typography.labelMedium)
            Text(
                "$monthly",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun HistoryCard(session: Session, dateText: String) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🕓 ${session.duration} min session",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
