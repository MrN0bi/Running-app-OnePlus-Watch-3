package com.watchrunning.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSession(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY startEpochMillis DESC LIMIT 1")
    suspend fun getInProgressSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE status != 'IN_PROGRESS' ORDER BY startEpochMillis DESC LIMIT :limit")
    fun recentSessions(limit: Int = 5): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoints(points: List<RoutePointEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPause(period: PausePeriodEntity)

    @Query("SELECT * FROM pause_periods WHERE sessionId = :sessionId ORDER BY sequence")
    suspend fun pauses(sessionId: String): List<PausePeriodEntity>

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY sequence")
    suspend fun routePoints(sessionId: String): List<RoutePointEntity>

    @Transaction
    suspend fun finalizeSession(
        session: WorkoutSessionEntity,
        routePoints: List<RoutePointEntity>,
        pause: PausePeriodEntity?,
    ) {
        if (routePoints.isNotEmpty()) insertRoutePoints(routePoints)
        if (pause != null) insertPause(pause)
        updateSession(session)
    }
}
