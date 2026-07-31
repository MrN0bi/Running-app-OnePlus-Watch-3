package com.watchrunning.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class DatabaseConverters {
    @TypeConverter fun sessionStatus(value: String): SessionStatus = SessionStatus.valueOf(value)
    @TypeConverter fun sessionStatus(value: SessionStatus): String = value.name
    @TypeConverter fun distanceSource(value: String): DistanceSource = DistanceSource.valueOf(value)
    @TypeConverter fun distanceSource(value: DistanceSource): String = value.name
    @TypeConverter fun pauseReason(value: String): PauseReason = PauseReason.valueOf(value)
    @TypeConverter fun pauseReason(value: PauseReason): String = value.name
}

@Database(
    entities = [WorkoutSessionEntity::class, RoutePointEntity::class, PausePeriodEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var instance: WorkoutDatabase? = null

        fun get(context: Context): WorkoutDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WorkoutDatabase::class.java,
                "workouts.db",
            ).build().also { instance = it }
        }
    }
}
