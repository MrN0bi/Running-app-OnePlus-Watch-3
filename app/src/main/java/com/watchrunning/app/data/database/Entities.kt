package com.watchrunning.app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SessionStatus { IN_PROGRESS, COMPLETED, INTERRUPTED }
enum class DistanceSource { HEALTH_SERVICES, FILTERED_ROUTE, UNAVAILABLE }
enum class PauseReason { USER, AUTO }

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val status: SessionStatus,
    val startEpochMillis: Long,
    val endEpochMillis: Long? = null,
    val startMonotonicMillis: Long,
    val elapsedMillis: Long = 0,
    val activeMillis: Long = 0,
    val distanceMetres: Double = 0.0,
    val distanceSource: DistanceSource = DistanceSource.UNAVAILABLE,
    val averagePaceMillisPerKm: Long? = null,
    val averageHeartRate: Double? = null,
    val maximumHeartRate: Double? = null,
    val heartRateSampleSum: Double = 0.0,
    val heartRateSampleCount: Long = 0,
    val zone1Millis: Long = 0,
    val zone2Millis: Long = 0,
    val zone3Millis: Long = 0,
    val zone4Millis: Long = 0,
    val zone5Millis: Long = 0,
    val unclassifiedHeartRateMillis: Long = 0,
    val effectiveMaximumHeartRate: Int? = null,
    val zone1LowerBpm: Int? = null,
    val zone2LowerBpm: Int? = null,
    val zone3LowerBpm: Int? = null,
    val zone4LowerBpm: Int? = null,
    val zone5LowerBpm: Int? = null,
    val smoothingWindowSeconds: Int = 5,
    val gpsAvailable: Boolean = false,
    val heartRateAvailable: Boolean = false,
    val endReason: Int? = null,
    val lastRouteSequence: Long = 0,
    val lastCheckpointEpochMillis: Long = startEpochMillis,
    val recoveredPartial: Boolean = false,
)

@Entity(
    tableName = "route_points",
    primaryKeys = ["sessionId", "sequence"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class RoutePointEntity(
    val sessionId: String,
    val sequence: Long,
    val epochMillis: Long,
    val activeOffsetMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMetres: Double?,
    val bearingDegrees: Double?,
    val horizontalAccuracyMetres: Double,
    val continuitySegment: Int,
)

@Entity(
    tableName = "pause_periods",
    primaryKeys = ["sessionId", "sequence"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class PausePeriodEntity(
    val sessionId: String,
    val sequence: Long,
    val startEpochMillis: Long,
    val endEpochMillis: Long?,
    val startActiveMillis: Long,
    val endActiveMillis: Long?,
    val reason: PauseReason,
)
