package uk.ac.tees.mad.breathe.ui.presentation

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import uk.ac.tees.mad.breathe.R

/**
 * SplashScreen:
 * - shows animated logo (app_icon.png)
 * - fetches a quote (ZenQuotes)
 * - checks Firebase auth to route to "home" or "auth"
 *
 * Replace route names "home" and "auth" with your actual nav route constants.
 */
@Composable
fun SplashScreen(navController: NavController, onFinishDelayMs: Long = 1600L) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    var loadingQuote by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(900))
        }


        // wait a bit to show the animation and quote
        delay(onFinishDelayMs)

        // check Firebase auth
        //val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            navController.navigate("home") {
//                popUpTo("splash") { inclusive = true }
//            }
//        } else {
//            navController.navigate("auth") {
//                popUpTo("splash") { inclusive = true }
//            }
//        }
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
                    .size(160.dp).clip(RoundedCornerShape(48.dp))
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app),
                    contentDescription = "Breathe app icon",
                    modifier = Modifier.clip(RoundedCornerShape(48.dp)).fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (loadingQuote) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            } else {
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
}
