package uk.ac.tees.mad.breathe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.ac.tees.mad.breathe.MainViewModel
import uk.ac.tees.mad.breathe.ui.presentation.AuthScreen
import uk.ac.tees.mad.breathe.ui.presentation.HomeScreen
import uk.ac.tees.mad.breathe.ui.presentation.SessionScreen
import uk.ac.tees.mad.breathe.ui.presentation.SplashScreen

sealed class MainnavItems(val x: String) {
    object Splash : MainnavItems("splash")
    object Auth : MainnavItems("auth")
    object Home : MainnavItems("home")
    object Session : MainnavItems("session/{duration}")

    // Placeholder for future screens
    object History : MainnavItems("history")
    object Profile : MainnavItems("profile")
}

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    NavHost(navController = navController, startDestination = MainnavItems.Splash.x) {
        composable(MainnavItems.Splash.x) {
            SplashScreen(navController = navController, viewModel = viewModel)
        }
        composable(MainnavItems.Auth.x) {
            AuthScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(MainnavItems.Home.x) {
            HomeScreen(navController = navController, vm = viewModel)
        }
        composable(
            route = MainnavItems.Session.x,
            arguments = listOf(navArgument("duration") { type = NavType.IntType })
        ) { backStackEntry ->
            val duration = backStackEntry.arguments?.getInt("duration") ?: 1
            SessionScreen(
                duration = duration,
                viewModel = viewModel,
                onEnd = { navController.popBackStack(MainnavItems.Home.x, false) } // Pop back to Home
            )
        }
        // Placeholder for future screens (uncomment when implemented)
        /*
        composable(MainnavItems.History.x) {
            HistoryScreen(navController = navController, viewModel = viewModel)
        }
        composable(MainnavItems.Profile.x) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        */
    }
}