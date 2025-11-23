package uk.ac.tees.mad.breathe.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration: Int,
    val timestamp: Long,
    val averageNoiseLevel: Float,
    val completed: Boolean
)