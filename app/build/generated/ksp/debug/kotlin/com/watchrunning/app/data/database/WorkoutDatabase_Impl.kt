package com.watchrunning.app.`data`.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WorkoutDatabase_Impl : WorkoutDatabase() {
  private val _workoutDao: Lazy<WorkoutDao> = lazy {
    WorkoutDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "bbcf00190f12c9f3b8f0d5281672eb4d", "32cf391672282660b3cbd82e636d8f4c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `status` TEXT NOT NULL, `startEpochMillis` INTEGER NOT NULL, `endEpochMillis` INTEGER, `startMonotonicMillis` INTEGER NOT NULL, `elapsedMillis` INTEGER NOT NULL, `activeMillis` INTEGER NOT NULL, `distanceMetres` REAL NOT NULL, `distanceSource` TEXT NOT NULL, `averagePaceMillisPerKm` INTEGER, `averageHeartRate` REAL, `maximumHeartRate` REAL, `heartRateSampleSum` REAL NOT NULL, `heartRateSampleCount` INTEGER NOT NULL, `zone1Millis` INTEGER NOT NULL, `zone2Millis` INTEGER NOT NULL, `zone3Millis` INTEGER NOT NULL, `zone4Millis` INTEGER NOT NULL, `zone5Millis` INTEGER NOT NULL, `unclassifiedHeartRateMillis` INTEGER NOT NULL, `effectiveMaximumHeartRate` INTEGER, `zone1LowerBpm` INTEGER, `zone2LowerBpm` INTEGER, `zone3LowerBpm` INTEGER, `zone4LowerBpm` INTEGER, `zone5LowerBpm` INTEGER, `smoothingWindowSeconds` INTEGER NOT NULL, `gpsAvailable` INTEGER NOT NULL, `heartRateAvailable` INTEGER NOT NULL, `endReason` INTEGER, `lastRouteSequence` INTEGER NOT NULL, `lastCheckpointEpochMillis` INTEGER NOT NULL, `recoveredPartial` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `route_points` (`sessionId` TEXT NOT NULL, `sequence` INTEGER NOT NULL, `epochMillis` INTEGER NOT NULL, `activeOffsetMillis` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `altitudeMetres` REAL, `bearingDegrees` REAL, `horizontalAccuracyMetres` REAL NOT NULL, `continuitySegment` INTEGER NOT NULL, PRIMARY KEY(`sessionId`, `sequence`), FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_route_points_sessionId` ON `route_points` (`sessionId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `pause_periods` (`sessionId` TEXT NOT NULL, `sequence` INTEGER NOT NULL, `startEpochMillis` INTEGER NOT NULL, `endEpochMillis` INTEGER, `startActiveMillis` INTEGER NOT NULL, `endActiveMillis` INTEGER, `reason` TEXT NOT NULL, PRIMARY KEY(`sessionId`, `sequence`), FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_pause_periods_sessionId` ON `pause_periods` (`sessionId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bbcf00190f12c9f3b8f0d5281672eb4d')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `workout_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `route_points`")
        connection.execSQL("DROP TABLE IF EXISTS `pause_periods`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsWorkoutSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("startEpochMillis", TableInfo.Column("startEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("endEpochMillis", TableInfo.Column("endEpochMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("startMonotonicMillis", TableInfo.Column("startMonotonicMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("elapsedMillis", TableInfo.Column("elapsedMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("activeMillis", TableInfo.Column("activeMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("distanceMetres", TableInfo.Column("distanceMetres", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("distanceSource", TableInfo.Column("distanceSource", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("averagePaceMillisPerKm", TableInfo.Column("averagePaceMillisPerKm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("averageHeartRate", TableInfo.Column("averageHeartRate", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("maximumHeartRate", TableInfo.Column("maximumHeartRate", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("heartRateSampleSum", TableInfo.Column("heartRateSampleSum", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("heartRateSampleCount", TableInfo.Column("heartRateSampleCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone1Millis", TableInfo.Column("zone1Millis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone2Millis", TableInfo.Column("zone2Millis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone3Millis", TableInfo.Column("zone3Millis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone4Millis", TableInfo.Column("zone4Millis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone5Millis", TableInfo.Column("zone5Millis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("unclassifiedHeartRateMillis", TableInfo.Column("unclassifiedHeartRateMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("effectiveMaximumHeartRate", TableInfo.Column("effectiveMaximumHeartRate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone1LowerBpm", TableInfo.Column("zone1LowerBpm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone2LowerBpm", TableInfo.Column("zone2LowerBpm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone3LowerBpm", TableInfo.Column("zone3LowerBpm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone4LowerBpm", TableInfo.Column("zone4LowerBpm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("zone5LowerBpm", TableInfo.Column("zone5LowerBpm", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("smoothingWindowSeconds", TableInfo.Column("smoothingWindowSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("gpsAvailable", TableInfo.Column("gpsAvailable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("heartRateAvailable", TableInfo.Column("heartRateAvailable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("endReason", TableInfo.Column("endReason", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("lastRouteSequence", TableInfo.Column("lastRouteSequence", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("lastCheckpointEpochMillis", TableInfo.Column("lastCheckpointEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSessions.put("recoveredPartial", TableInfo.Column("recoveredPartial", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWorkoutSessions: TableInfo = TableInfo("workout_sessions", _columnsWorkoutSessions, _foreignKeysWorkoutSessions, _indicesWorkoutSessions)
        val _existingWorkoutSessions: TableInfo = read(connection, "workout_sessions")
        if (!_infoWorkoutSessions.equals(_existingWorkoutSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_sessions(com.watchrunning.app.data.database.WorkoutSessionEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutSessions + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutSessions)
        }
        val _columnsRoutePoints: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRoutePoints.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("sequence", TableInfo.Column("sequence", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("epochMillis", TableInfo.Column("epochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("activeOffsetMillis", TableInfo.Column("activeOffsetMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("latitude", TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("longitude", TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("altitudeMetres", TableInfo.Column("altitudeMetres", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("bearingDegrees", TableInfo.Column("bearingDegrees", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("horizontalAccuracyMetres", TableInfo.Column("horizontalAccuracyMetres", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRoutePoints.put("continuitySegment", TableInfo.Column("continuitySegment", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRoutePoints: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysRoutePoints.add(TableInfo.ForeignKey("workout_sessions", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesRoutePoints: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRoutePoints.add(TableInfo.Index("index_route_points_sessionId", false, listOf("sessionId"), listOf("ASC")))
        val _infoRoutePoints: TableInfo = TableInfo("route_points", _columnsRoutePoints, _foreignKeysRoutePoints, _indicesRoutePoints)
        val _existingRoutePoints: TableInfo = read(connection, "route_points")
        if (!_infoRoutePoints.equals(_existingRoutePoints)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |route_points(com.watchrunning.app.data.database.RoutePointEntity).
              | Expected:
              |""".trimMargin() + _infoRoutePoints + """
              |
              | Found:
              |""".trimMargin() + _existingRoutePoints)
        }
        val _columnsPausePeriods: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPausePeriods.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("sequence", TableInfo.Column("sequence", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("startEpochMillis", TableInfo.Column("startEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("endEpochMillis", TableInfo.Column("endEpochMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("startActiveMillis", TableInfo.Column("startActiveMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("endActiveMillis", TableInfo.Column("endActiveMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPausePeriods.put("reason", TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPausePeriods: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysPausePeriods.add(TableInfo.ForeignKey("workout_sessions", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesPausePeriods: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPausePeriods.add(TableInfo.Index("index_pause_periods_sessionId", false, listOf("sessionId"), listOf("ASC")))
        val _infoPausePeriods: TableInfo = TableInfo("pause_periods", _columnsPausePeriods, _foreignKeysPausePeriods, _indicesPausePeriods)
        val _existingPausePeriods: TableInfo = read(connection, "pause_periods")
        if (!_infoPausePeriods.equals(_existingPausePeriods)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |pause_periods(com.watchrunning.app.data.database.PausePeriodEntity).
              | Expected:
              |""".trimMargin() + _infoPausePeriods + """
              |
              | Found:
              |""".trimMargin() + _existingPausePeriods)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "workout_sessions", "route_points", "pause_periods")
  }

  public override fun clearAllTables() {
    super.performClear(true, "workout_sessions", "route_points", "pause_periods")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WorkoutDao::class, WorkoutDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun workoutDao(): WorkoutDao = _workoutDao.value
}
