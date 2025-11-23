package uk.ac.tees.mad.breathe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.breathe.MainViewModel
import uk.ac.tees.mad.breathe.ui.presentation.AuthScreen
import uk.ac.tees.mad.breathe.ui.presentation.HomeScreen
import uk.ac.tees.mad.breathe.ui.presentation.SplashScreen

sealed class MainnavItems(val x : String){
    object Splash : MainnavItems("splash")
    object Auth : MainnavItems("auth")
    object Home : MainnavItems("home")
}

@Composable
fun MainNavGraph(){
    val navController = rememberNavController()
    val viewModel : MainViewModel = hiltViewModel()
    NavHost(navController, startDestination = MainnavItems.Splash.x){
        composable(MainnavItems.Splash.x) {
            SplashScreen(navController, viewModel = viewModel)
        }
        composable(MainnavItems.Auth.x) {
            AuthScreen(
                navController,
                viewModel = viewModel
            )
        }
        composable(MainnavItems.Home.x) {
            HomeScreen(navController, viewModel)
        }
        }
}