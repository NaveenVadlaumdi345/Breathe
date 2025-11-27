package uk.ac.tees.mad.breathe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Rounded.Home, "Home")
    object History : BottomNavItem("history", Icons.Rounded.History, "History")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person2, "Profile")
}


@Composable
fun FloatingBottomBar(
    navController: NavController,
    currentRoute: String
) {
    val items = listOf(
        NavItem("home", Icons.Rounded.Home, "Home"),
        NavItem("history", Icons.Rounded.History, "History"),
        NavItem("profile", Icons.Rounded.Person, "Profile")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(50))
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                FloatingNavItem(
                    item = item,
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun FloatingNavItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 48.dp else 40.dp)
                .background(
                    brush = if (selected)
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    else
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))
        if (selected) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
