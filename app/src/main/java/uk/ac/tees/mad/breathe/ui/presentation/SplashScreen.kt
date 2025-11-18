package uk.ac.tees.mad.breathe.ui.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.ac.tees.mad.breathe.MainViewModel
import uk.ac.tees.mad.breathe.R
import uk.ac.tees.mad.breathe.ui.navigation.MainnavItems

@Composable
fun SplashScreen(
    navController: NavController,
    onFinishDelayMs: Long = 1600L,
    viewModel: MainViewModel
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == null) return@LaunchedEffect

        delay(onFinishDelayMs)
        if (isLoggedIn == true) {
            navController.navigate(MainnavItems.Home.x) {
                popUpTo(0)
            }
        } else {
            navController.navigate(MainnavItems.Auth.x) {
                popUpTo(0)
            }
        }
    }


    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(900))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app),
                    contentDescription = "Breathe app icon",
                    modifier = Modifier
                        .clip(RoundedCornerShape(48.dp))
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Breathe. Be present.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }
    }
}
