package uk.ac.tees.mad.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.ac.tees.mad.breathe.data.model.Session

@Database(entities = [Session::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
