package uk.ac.tees.mad.breathe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.ac.tees.mad.breathe.data.model.Session

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    suspend fun getAll(): List<Session>
}
