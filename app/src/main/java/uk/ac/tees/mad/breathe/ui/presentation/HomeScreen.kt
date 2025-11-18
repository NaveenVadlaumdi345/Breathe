package uk.ac.tees.mad.breathe.ui.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    Text(text = "Home Screen", fontSize = 54.sp)
}
