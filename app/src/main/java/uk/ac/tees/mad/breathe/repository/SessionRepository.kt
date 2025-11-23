package uk.ac.tees.mad.breathe.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import uk.ac.tees.mad.breathe.data.local.SessionDao
import uk.ac.tees.mad.breathe.data.model.Session
import uk.ac.tees.mad.breathe.network.AssemblyApi
import uk.ac.tees.mad.breathe.network.AudioAnalysisRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val dao: SessionDao,
    private val assemblyApi: AssemblyApi
) {
    suspend fun analyzeNoise(base64Audio: String): Float {
        return try {
            val response = assemblyApi.analyzeNoise(AudioAnalysisRequest(base64Audio))
            response.noise_level ?: 0f
        } catch (e: Exception) {
            0f
        }
    }

    suspend fun saveSession(session: Session) {
        dao.insert(session)
        val user = auth.currentUser ?: return
        db.getReference("users").child(user.uid)
            .child("sessions").push().setValue(session)
    }

    suspend fun getAllSessions(): List<Session> = dao.getAll()
}
