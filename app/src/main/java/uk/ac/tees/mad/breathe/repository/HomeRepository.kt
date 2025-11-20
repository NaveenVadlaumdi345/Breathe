package uk.ac.tees.mad.breathe.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.breathe.network.ZenQuotesApi
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val ambientNoiseDetection: Boolean = false,
    val defaultDurationMinutes: Int = 3
)

@Singleton
class HomeRepository @Inject constructor(
    private val api: ZenQuotesApi,
    private val auth: FirebaseAuth,
) {
    // Fetch a random quote from ZenQuotes
    suspend fun fetchRandomQuote(): Result<Quote> {
        return try {
            val list = api.getRandomQuote()
            if (list.isNullOrEmpty()) {
                Result.failure(Exception("No quote received"))
            } else {
                val r = list[0]
                val q = Quote(text = r.q ?: "Be present.", author = r.a ?: "Unknown")
                Result.success(q)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Read preferences from Realtime Database at /users/{uid}/preferences
    suspend fun getPreferences(): Result<UserPreferences> {
        val user = auth.currentUser ?: return Result.success(UserPreferences())
        return try {
            val ref = db.getReference("users").child(user.uid).child("preferences")
            val snap = ref.get().await()
            val ambient = snap.child("ambientNoiseDetection").getValue(Boolean::class.java) ?: false
            val duration = snap.child("defaultDurationMinutes").getValue(Int::class.java) ?: 3
            Result.success(UserPreferences(ambient, duration))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update preferences at /users/{uid}/preferences
    suspend fun savePreferences(prefs: UserPreferences): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = db.getReference("users").child(user.uid).child("preferences")
            val map = mapOf(
                "ambientNoiseDetection" to prefs.ambientNoiseDetection,
                "defaultDurationMinutes" to prefs.defaultDurationMinutes
            )
            ref.updateChildren(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
