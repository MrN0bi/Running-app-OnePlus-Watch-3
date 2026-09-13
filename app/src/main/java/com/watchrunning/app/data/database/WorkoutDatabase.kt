package com.watchrunning.app.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec

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
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WorkoutDatabase.RemoveRetiredMetricsMigration::class),
    ],
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "workout_sessions", columnName = "zone1Millis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone2Millis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone3Millis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone4Millis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone5Millis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "unclassifiedHeartRateMillis"),
        DeleteColumn(tableName = "workout_sessions", columnName = "effectiveMaximumHeartRate"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone1LowerBpm"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone2LowerBpm"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone3LowerBpm"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone4LowerBpm"),
        DeleteColumn(tableName = "workout_sessions", columnName = "zone5LowerBpm"),
    )
    class RemoveRetiredMetricsMigration : AutoMigrationSpec

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
