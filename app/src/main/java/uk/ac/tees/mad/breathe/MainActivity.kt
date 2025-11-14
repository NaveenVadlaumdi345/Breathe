package uk.ac.tees.mad.breathe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import uk.ac.tees.mad.breathe.ui.theme.BreatheTheme
import uk.ac.tees.mad.breathe.ui.presentation.SplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreatheTheme {
                val navController = rememberNavController()
                SplashScreen(navController)
            }
        }
    }
}
