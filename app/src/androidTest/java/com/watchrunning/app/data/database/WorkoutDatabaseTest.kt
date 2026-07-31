package com.watchrunning.app.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDatabaseTest {
    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        WorkoutDatabase::class.java,
    ).build()

    @After fun closeDatabase() = database.close()

    @Test fun finalizationStoresSessionRouteAndPauseAtomically() = runBlocking {
        val session = WorkoutSessionEntity(
            id = "run-1",
            status = SessionStatus.IN_PROGRESS,
            startEpochMillis = 1_000,
            startMonotonicMillis = 500,
        )
        database.workoutDao().insertSession(session)
        val route = RoutePointEntity(
            sessionId = session.id,
            sequence = 1,
            epochMillis = 2_000,
            activeOffsetMillis = 1_000,
            latitude = 48.8566,
            longitude = 2.3522,
            altitudeMetres = null,
            bearingDegrees = null,
            horizontalAccuracyMetres = 5.0,
            continuitySegment = 0,
        )
        val pause = PausePeriodEntity(
            sessionId = session.id,
            sequence = 1,
            startEpochMillis = 3_000,
            endEpochMillis = 4_000,
            startActiveMillis = 2_000,
            endActiveMillis = 2_000,
            reason = PauseReason.USER,
        )
        database.workoutDao().finalizeSession(
            session.copy(status = SessionStatus.COMPLETED, endEpochMillis = 5_000),
            listOf(route),
            pause,
        )

        assertEquals(SessionStatus.COMPLETED, database.workoutDao().getSession(session.id)?.status)
        assertEquals(listOf(route), database.workoutDao().routePoints(session.id))
        assertEquals(listOf(pause), database.workoutDao().pauses(session.id))
        assertNotNull(database.workoutDao().getSession(session.id)?.endEpochMillis)
    }
}
