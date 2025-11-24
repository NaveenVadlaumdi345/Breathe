package uk.ac.tees.mad.breathe.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import uk.ac.tees.mad.breathe.data.local.SessionDao
import uk.ac.tees.mad.breathe.data.model.Session
import javax.inject.Inject

class SessionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val dao: SessionDao,
) {


    suspend fun saveSession(session: Session) {
        dao.insert(session)
        val user = auth.currentUser ?: return
        db.getReference("users").child(user.uid)
            .child("sessions").push().setValue(session)
    }

    suspend fun getAllSessions(): List<Session> = dao.getAll()
}