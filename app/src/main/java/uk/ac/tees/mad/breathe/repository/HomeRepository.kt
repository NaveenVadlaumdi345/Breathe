package uk.ac.tees.mad.breathe.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import uk.ac.tees.mad.breathe.network.ZenQuotesApi
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.breathe.data.model.Quote
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val ambientNoiseDetection: Boolean = false,
    val defaultDurationMinutes: Int = 3
)

class HomeRepository @Inject constructor(
    private val api: ZenQuotesApi,
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase
) {
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
            Log.d("HomeRepository", "Exception: $e")
            Result.failure(e)
        }
    }

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
