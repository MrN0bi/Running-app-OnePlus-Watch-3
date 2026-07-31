package com.watchrunning.app.`data`.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WorkoutDao_Impl(
  __db: RoomDatabase,
) : WorkoutDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutSessionEntity: EntityInsertAdapter<WorkoutSessionEntity>

  private val __databaseConverters: DatabaseConverters = DatabaseConverters()

  private val __insertAdapterOfRoutePointEntity: EntityInsertAdapter<RoutePointEntity>

  private val __insertAdapterOfPausePeriodEntity: EntityInsertAdapter<PausePeriodEntity>

  private val __updateAdapterOfWorkoutSessionEntity:
      EntityDeleteOrUpdateAdapter<WorkoutSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutSessionEntity = object : EntityInsertAdapter<WorkoutSessionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `workout_sessions` (`id`,`status`,`startEpochMillis`,`endEpochMillis`,`startMonotonicMillis`,`elapsedMillis`,`activeMillis`,`distanceMetres`,`distanceSource`,`averagePaceMillisPerKm`,`averageHeartRate`,`maximumHeartRate`,`heartRateSampleSum`,`heartRateSampleCount`,`zone1Millis`,`zone2Millis`,`zone3Millis`,`zone4Millis`,`zone5Millis`,`unclassifiedHeartRateMillis`,`effectiveMaximumHeartRate`,`zone1LowerBpm`,`zone2LowerBpm`,`zone3LowerBpm`,`zone4LowerBpm`,`zone5LowerBpm`,`smoothingWindowSeconds`,`gpsAvailable`,`heartRateAvailable`,`endReason`,`lastRouteSequence`,`lastCheckpointEpochMillis`,`recoveredPartial`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSessionEntity) {
        statement.bindText(1, entity.id)
        val _tmp: String = __databaseConverters.sessionStatus(entity.status)
        statement.bindText(2, _tmp)
        statement.bindLong(3, entity.startEpochMillis)
        val _tmpEndEpochMillis: Long? = entity.endEpochMillis
        if (_tmpEndEpochMillis == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndEpochMillis)
        }
        statement.bindLong(5, entity.startMonotonicMillis)
        statement.bindLong(6, entity.elapsedMillis)
        statement.bindLong(7, entity.activeMillis)
        statement.bindDouble(8, entity.distanceMetres)
        val _tmp_1: String = __databaseConverters.distanceSource(entity.distanceSource)
        statement.bindText(9, _tmp_1)
        val _tmpAveragePaceMillisPerKm: Long? = entity.averagePaceMillisPerKm
        if (_tmpAveragePaceMillisPerKm == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAveragePaceMillisPerKm)
        }
        val _tmpAverageHeartRate: Double? = entity.averageHeartRate
        if (_tmpAverageHeartRate == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpAverageHeartRate)
        }
        val _tmpMaximumHeartRate: Double? = entity.maximumHeartRate
        if (_tmpMaximumHeartRate == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpMaximumHeartRate)
        }
        statement.bindDouble(13, entity.heartRateSampleSum)
        statement.bindLong(14, entity.heartRateSampleCount)
        statement.bindLong(15, entity.zone1Millis)
        statement.bindLong(16, entity.zone2Millis)
        statement.bindLong(17, entity.zone3Millis)
        statement.bindLong(18, entity.zone4Millis)
        statement.bindLong(19, entity.zone5Millis)
        statement.bindLong(20, entity.unclassifiedHeartRateMillis)
        val _tmpEffectiveMaximumHeartRate: Int? = entity.effectiveMaximumHeartRate
        if (_tmpEffectiveMaximumHeartRate == null) {
          statement.bindNull(21)
        } else {
          statement.bindLong(21, _tmpEffectiveMaximumHeartRate.toLong())
        }
        val _tmpZone1LowerBpm: Int? = entity.zone1LowerBpm
        if (_tmpZone1LowerBpm == null) {
          statement.bindNull(22)
        } else {
          statement.bindLong(22, _tmpZone1LowerBpm.toLong())
        }
        val _tmpZone2LowerBpm: Int? = entity.zone2LowerBpm
        if (_tmpZone2LowerBpm == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpZone2LowerBpm.toLong())
        }
        val _tmpZone3LowerBpm: Int? = entity.zone3LowerBpm
        if (_tmpZone3LowerBpm == null) {
          statement.bindNull(24)
        } else {
          statement.bindLong(24, _tmpZone3LowerBpm.toLong())
        }
        val _tmpZone4LowerBpm: Int? = entity.zone4LowerBpm
        if (_tmpZone4LowerBpm == null) {
          statement.bindNull(25)
        } else {
          statement.bindLong(25, _tmpZone4LowerBpm.toLong())
        }
        val _tmpZone5LowerBpm: Int? = entity.zone5LowerBpm
        if (_tmpZone5LowerBpm == null) {
          statement.bindNull(26)
        } else {
          statement.bindLong(26, _tmpZone5LowerBpm.toLong())
        }
        statement.bindLong(27, entity.smoothingWindowSeconds.toLong())
        val _tmp_2: Int = if (entity.gpsAvailable) 1 else 0
        statement.bindLong(28, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.heartRateAvailable) 1 else 0
        statement.bindLong(29, _tmp_3.toLong())
        val _tmpEndReason: Int? = entity.endReason
        if (_tmpEndReason == null) {
          statement.bindNull(30)
        } else {
          statement.bindLong(30, _tmpEndReason.toLong())
        }
        statement.bindLong(31, entity.lastRouteSequence)
        statement.bindLong(32, entity.lastCheckpointEpochMillis)
        val _tmp_4: Int = if (entity.recoveredPartial) 1 else 0
        statement.bindLong(33, _tmp_4.toLong())
      }
    }
    this.__insertAdapterOfRoutePointEntity = object : EntityInsertAdapter<RoutePointEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `route_points` (`sessionId`,`sequence`,`epochMillis`,`activeOffsetMillis`,`latitude`,`longitude`,`altitudeMetres`,`bearingDegrees`,`horizontalAccuracyMetres`,`continuitySegment`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RoutePointEntity) {
        statement.bindText(1, entity.sessionId)
        statement.bindLong(2, entity.sequence)
        statement.bindLong(3, entity.epochMillis)
        statement.bindLong(4, entity.activeOffsetMillis)
        statement.bindDouble(5, entity.latitude)
        statement.bindDouble(6, entity.longitude)
        val _tmpAltitudeMetres: Double? = entity.altitudeMetres
        if (_tmpAltitudeMetres == null) {
          statement.bindNull(7)
        } else {
          statement.bindDouble(7, _tmpAltitudeMetres)
        }
        val _tmpBearingDegrees: Double? = entity.bearingDegrees
        if (_tmpBearingDegrees == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpBearingDegrees)
        }
        statement.bindDouble(9, entity.horizontalAccuracyMetres)
        statement.bindLong(10, entity.continuitySegment.toLong())
      }
    }
    this.__insertAdapterOfPausePeriodEntity = object : EntityInsertAdapter<PausePeriodEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `pause_periods` (`sessionId`,`sequence`,`startEpochMillis`,`endEpochMillis`,`startActiveMillis`,`endActiveMillis`,`reason`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PausePeriodEntity) {
        statement.bindText(1, entity.sessionId)
        statement.bindLong(2, entity.sequence)
        statement.bindLong(3, entity.startEpochMillis)
        val _tmpEndEpochMillis: Long? = entity.endEpochMillis
        if (_tmpEndEpochMillis == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndEpochMillis)
        }
        statement.bindLong(5, entity.startActiveMillis)
        val _tmpEndActiveMillis: Long? = entity.endActiveMillis
        if (_tmpEndActiveMillis == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndActiveMillis)
        }
        val _tmp: String = __databaseConverters.pauseReason(entity.reason)
        statement.bindText(7, _tmp)
      }
    }
    this.__updateAdapterOfWorkoutSessionEntity = object : EntityDeleteOrUpdateAdapter<WorkoutSessionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `workout_sessions` SET `id` = ?,`status` = ?,`startEpochMillis` = ?,`endEpochMillis` = ?,`startMonotonicMillis` = ?,`elapsedMillis` = ?,`activeMillis` = ?,`distanceMetres` = ?,`distanceSource` = ?,`averagePaceMillisPerKm` = ?,`averageHeartRate` = ?,`maximumHeartRate` = ?,`heartRateSampleSum` = ?,`heartRateSampleCount` = ?,`zone1Millis` = ?,`zone2Millis` = ?,`zone3Millis` = ?,`zone4Millis` = ?,`zone5Millis` = ?,`unclassifiedHeartRateMillis` = ?,`effectiveMaximumHeartRate` = ?,`zone1LowerBpm` = ?,`zone2LowerBpm` = ?,`zone3LowerBpm` = ?,`zone4LowerBpm` = ?,`zone5LowerBpm` = ?,`smoothingWindowSeconds` = ?,`gpsAvailable` = ?,`heartRateAvailable` = ?,`endReason` = ?,`lastRouteSequence` = ?,`lastCheckpointEpochMillis` = ?,`recoveredPartial` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSessionEntity) {
        statement.bindText(1, entity.id)
        val _tmp: String = __databaseConverters.sessionStatus(entity.status)
        statement.bindText(2, _tmp)
        statement.bindLong(3, entity.startEpochMillis)
        val _tmpEndEpochMillis: Long? = entity.endEpochMillis
        if (_tmpEndEpochMillis == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndEpochMillis)
        }
        statement.bindLong(5, entity.startMonotonicMillis)
        statement.bindLong(6, entity.elapsedMillis)
        statement.bindLong(7, entity.activeMillis)
        statement.bindDouble(8, entity.distanceMetres)
        val _tmp_1: String = __databaseConverters.distanceSource(entity.distanceSource)
        statement.bindText(9, _tmp_1)
        val _tmpAveragePaceMillisPerKm: Long? = entity.averagePaceMillisPerKm
        if (_tmpAveragePaceMillisPerKm == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAveragePaceMillisPerKm)
        }
        val _tmpAverageHeartRate: Double? = entity.averageHeartRate
        if (_tmpAverageHeartRate == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpAverageHeartRate)
        }
        val _tmpMaximumHeartRate: Double? = entity.maximumHeartRate
        if (_tmpMaximumHeartRate == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpMaximumHeartRate)
        }
        statement.bindDouble(13, entity.heartRateSampleSum)
        statement.bindLong(14, entity.heartRateSampleCount)
        statement.bindLong(15, entity.zone1Millis)
        statement.bindLong(16, entity.zone2Millis)
        statement.bindLong(17, entity.zone3Millis)
        statement.bindLong(18, entity.zone4Millis)
        statement.bindLong(19, entity.zone5Millis)
        statement.bindLong(20, entity.unclassifiedHeartRateMillis)
        val _tmpEffectiveMaximumHeartRate: Int? = entity.effectiveMaximumHeartRate
        if (_tmpEffectiveMaximumHeartRate == null) {
          statement.bindNull(21)
        } else {
          statement.bindLong(21, _tmpEffectiveMaximumHeartRate.toLong())
        }
        val _tmpZone1LowerBpm: Int? = entity.zone1LowerBpm
        if (_tmpZone1LowerBpm == null) {
          statement.bindNull(22)
        } else {
          statement.bindLong(22, _tmpZone1LowerBpm.toLong())
        }
        val _tmpZone2LowerBpm: Int? = entity.zone2LowerBpm
        if (_tmpZone2LowerBpm == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpZone2LowerBpm.toLong())
        }
        val _tmpZone3LowerBpm: Int? = entity.zone3LowerBpm
        if (_tmpZone3LowerBpm == null) {
          statement.bindNull(24)
        } else {
          statement.bindLong(24, _tmpZone3LowerBpm.toLong())
        }
        val _tmpZone4LowerBpm: Int? = entity.zone4LowerBpm
        if (_tmpZone4LowerBpm == null) {
          statement.bindNull(25)
        } else {
          statement.bindLong(25, _tmpZone4LowerBpm.toLong())
        }
        val _tmpZone5LowerBpm: Int? = entity.zone5LowerBpm
        if (_tmpZone5LowerBpm == null) {
          statement.bindNull(26)
        } else {
          statement.bindLong(26, _tmpZone5LowerBpm.toLong())
        }
        statement.bindLong(27, entity.smoothingWindowSeconds.toLong())
        val _tmp_2: Int = if (entity.gpsAvailable) 1 else 0
        statement.bindLong(28, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.heartRateAvailable) 1 else 0
        statement.bindLong(29, _tmp_3.toLong())
        val _tmpEndReason: Int? = entity.endReason
        if (_tmpEndReason == null) {
          statement.bindNull(30)
        } else {
          statement.bindLong(30, _tmpEndReason.toLong())
        }
        statement.bindLong(31, entity.lastRouteSequence)
        statement.bindLong(32, entity.lastCheckpointEpochMillis)
        val _tmp_4: Int = if (entity.recoveredPartial) 1 else 0
        statement.bindLong(33, _tmp_4.toLong())
        statement.bindText(34, entity.id)
      }
    }
  }

  public override suspend fun insertSession(session: WorkoutSessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWorkoutSessionEntity.insert(_connection, session)
  }

  public override suspend fun insertRoutePoints(points: List<RoutePointEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRoutePointEntity.insert(_connection, points)
  }

  public override suspend fun insertPause(period: PausePeriodEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPausePeriodEntity.insert(_connection, period)
  }

  public override suspend fun updateSession(session: WorkoutSessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfWorkoutSessionEntity.handle(_connection, session)
  }

  public override suspend fun finalizeSession(
    session: WorkoutSessionEntity,
    routePoints: List<RoutePointEntity>,
    pause: PausePeriodEntity?,
  ): Unit = performInTransactionSuspending(__db) {
    super@WorkoutDao_Impl.finalizeSession(session, routePoints, pause)
  }

  public override suspend fun getSession(id: String): WorkoutSessionEntity? {
    val _sql: String = "SELECT * FROM workout_sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfStartEpochMillis: Int = getColumnIndexOrThrow(_stmt, "startEpochMillis")
        val _columnIndexOfEndEpochMillis: Int = getColumnIndexOrThrow(_stmt, "endEpochMillis")
        val _columnIndexOfStartMonotonicMillis: Int = getColumnIndexOrThrow(_stmt, "startMonotonicMillis")
        val _columnIndexOfElapsedMillis: Int = getColumnIndexOrThrow(_stmt, "elapsedMillis")
        val _columnIndexOfActiveMillis: Int = getColumnIndexOrThrow(_stmt, "activeMillis")
        val _columnIndexOfDistanceMetres: Int = getColumnIndexOrThrow(_stmt, "distanceMetres")
        val _columnIndexOfDistanceSource: Int = getColumnIndexOrThrow(_stmt, "distanceSource")
        val _columnIndexOfAveragePaceMillisPerKm: Int = getColumnIndexOrThrow(_stmt, "averagePaceMillisPerKm")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "maximumHeartRate")
        val _columnIndexOfHeartRateSampleSum: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleSum")
        val _columnIndexOfHeartRateSampleCount: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleCount")
        val _columnIndexOfZone1Millis: Int = getColumnIndexOrThrow(_stmt, "zone1Millis")
        val _columnIndexOfZone2Millis: Int = getColumnIndexOrThrow(_stmt, "zone2Millis")
        val _columnIndexOfZone3Millis: Int = getColumnIndexOrThrow(_stmt, "zone3Millis")
        val _columnIndexOfZone4Millis: Int = getColumnIndexOrThrow(_stmt, "zone4Millis")
        val _columnIndexOfZone5Millis: Int = getColumnIndexOrThrow(_stmt, "zone5Millis")
        val _columnIndexOfUnclassifiedHeartRateMillis: Int = getColumnIndexOrThrow(_stmt, "unclassifiedHeartRateMillis")
        val _columnIndexOfEffectiveMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "effectiveMaximumHeartRate")
        val _columnIndexOfZone1LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone1LowerBpm")
        val _columnIndexOfZone2LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone2LowerBpm")
        val _columnIndexOfZone3LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone3LowerBpm")
        val _columnIndexOfZone4LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone4LowerBpm")
        val _columnIndexOfZone5LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone5LowerBpm")
        val _columnIndexOfSmoothingWindowSeconds: Int = getColumnIndexOrThrow(_stmt, "smoothingWindowSeconds")
        val _columnIndexOfGpsAvailable: Int = getColumnIndexOrThrow(_stmt, "gpsAvailable")
        val _columnIndexOfHeartRateAvailable: Int = getColumnIndexOrThrow(_stmt, "heartRateAvailable")
        val _columnIndexOfEndReason: Int = getColumnIndexOrThrow(_stmt, "endReason")
        val _columnIndexOfLastRouteSequence: Int = getColumnIndexOrThrow(_stmt, "lastRouteSequence")
        val _columnIndexOfLastCheckpointEpochMillis: Int = getColumnIndexOrThrow(_stmt, "lastCheckpointEpochMillis")
        val _columnIndexOfRecoveredPartial: Int = getColumnIndexOrThrow(_stmt, "recoveredPartial")
        val _result: WorkoutSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpStatus: SessionStatus
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __databaseConverters.sessionStatus(_tmp)
          val _tmpStartEpochMillis: Long
          _tmpStartEpochMillis = _stmt.getLong(_columnIndexOfStartEpochMillis)
          val _tmpEndEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMillis)) {
            _tmpEndEpochMillis = null
          } else {
            _tmpEndEpochMillis = _stmt.getLong(_columnIndexOfEndEpochMillis)
          }
          val _tmpStartMonotonicMillis: Long
          _tmpStartMonotonicMillis = _stmt.getLong(_columnIndexOfStartMonotonicMillis)
          val _tmpElapsedMillis: Long
          _tmpElapsedMillis = _stmt.getLong(_columnIndexOfElapsedMillis)
          val _tmpActiveMillis: Long
          _tmpActiveMillis = _stmt.getLong(_columnIndexOfActiveMillis)
          val _tmpDistanceMetres: Double
          _tmpDistanceMetres = _stmt.getDouble(_columnIndexOfDistanceMetres)
          val _tmpDistanceSource: DistanceSource
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfDistanceSource)
          _tmpDistanceSource = __databaseConverters.distanceSource(_tmp_1)
          val _tmpAveragePaceMillisPerKm: Long?
          if (_stmt.isNull(_columnIndexOfAveragePaceMillisPerKm)) {
            _tmpAveragePaceMillisPerKm = null
          } else {
            _tmpAveragePaceMillisPerKm = _stmt.getLong(_columnIndexOfAveragePaceMillisPerKm)
          }
          val _tmpAverageHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getDouble(_columnIndexOfAverageHeartRate)
          }
          val _tmpMaximumHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfMaximumHeartRate)) {
            _tmpMaximumHeartRate = null
          } else {
            _tmpMaximumHeartRate = _stmt.getDouble(_columnIndexOfMaximumHeartRate)
          }
          val _tmpHeartRateSampleSum: Double
          _tmpHeartRateSampleSum = _stmt.getDouble(_columnIndexOfHeartRateSampleSum)
          val _tmpHeartRateSampleCount: Long
          _tmpHeartRateSampleCount = _stmt.getLong(_columnIndexOfHeartRateSampleCount)
          val _tmpZone1Millis: Long
          _tmpZone1Millis = _stmt.getLong(_columnIndexOfZone1Millis)
          val _tmpZone2Millis: Long
          _tmpZone2Millis = _stmt.getLong(_columnIndexOfZone2Millis)
          val _tmpZone3Millis: Long
          _tmpZone3Millis = _stmt.getLong(_columnIndexOfZone3Millis)
          val _tmpZone4Millis: Long
          _tmpZone4Millis = _stmt.getLong(_columnIndexOfZone4Millis)
          val _tmpZone5Millis: Long
          _tmpZone5Millis = _stmt.getLong(_columnIndexOfZone5Millis)
          val _tmpUnclassifiedHeartRateMillis: Long
          _tmpUnclassifiedHeartRateMillis = _stmt.getLong(_columnIndexOfUnclassifiedHeartRateMillis)
          val _tmpEffectiveMaximumHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfEffectiveMaximumHeartRate)) {
            _tmpEffectiveMaximumHeartRate = null
          } else {
            _tmpEffectiveMaximumHeartRate = _stmt.getLong(_columnIndexOfEffectiveMaximumHeartRate).toInt()
          }
          val _tmpZone1LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone1LowerBpm)) {
            _tmpZone1LowerBpm = null
          } else {
            _tmpZone1LowerBpm = _stmt.getLong(_columnIndexOfZone1LowerBpm).toInt()
          }
          val _tmpZone2LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone2LowerBpm)) {
            _tmpZone2LowerBpm = null
          } else {
            _tmpZone2LowerBpm = _stmt.getLong(_columnIndexOfZone2LowerBpm).toInt()
          }
          val _tmpZone3LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone3LowerBpm)) {
            _tmpZone3LowerBpm = null
          } else {
            _tmpZone3LowerBpm = _stmt.getLong(_columnIndexOfZone3LowerBpm).toInt()
          }
          val _tmpZone4LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone4LowerBpm)) {
            _tmpZone4LowerBpm = null
          } else {
            _tmpZone4LowerBpm = _stmt.getLong(_columnIndexOfZone4LowerBpm).toInt()
          }
          val _tmpZone5LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone5LowerBpm)) {
            _tmpZone5LowerBpm = null
          } else {
            _tmpZone5LowerBpm = _stmt.getLong(_columnIndexOfZone5LowerBpm).toInt()
          }
          val _tmpSmoothingWindowSeconds: Int
          _tmpSmoothingWindowSeconds = _stmt.getLong(_columnIndexOfSmoothingWindowSeconds).toInt()
          val _tmpGpsAvailable: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfGpsAvailable).toInt()
          _tmpGpsAvailable = _tmp_2 != 0
          val _tmpHeartRateAvailable: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfHeartRateAvailable).toInt()
          _tmpHeartRateAvailable = _tmp_3 != 0
          val _tmpEndReason: Int?
          if (_stmt.isNull(_columnIndexOfEndReason)) {
            _tmpEndReason = null
          } else {
            _tmpEndReason = _stmt.getLong(_columnIndexOfEndReason).toInt()
          }
          val _tmpLastRouteSequence: Long
          _tmpLastRouteSequence = _stmt.getLong(_columnIndexOfLastRouteSequence)
          val _tmpLastCheckpointEpochMillis: Long
          _tmpLastCheckpointEpochMillis = _stmt.getLong(_columnIndexOfLastCheckpointEpochMillis)
          val _tmpRecoveredPartial: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfRecoveredPartial).toInt()
          _tmpRecoveredPartial = _tmp_4 != 0
          _result = WorkoutSessionEntity(_tmpId,_tmpStatus,_tmpStartEpochMillis,_tmpEndEpochMillis,_tmpStartMonotonicMillis,_tmpElapsedMillis,_tmpActiveMillis,_tmpDistanceMetres,_tmpDistanceSource,_tmpAveragePaceMillisPerKm,_tmpAverageHeartRate,_tmpMaximumHeartRate,_tmpHeartRateSampleSum,_tmpHeartRateSampleCount,_tmpZone1Millis,_tmpZone2Millis,_tmpZone3Millis,_tmpZone4Millis,_tmpZone5Millis,_tmpUnclassifiedHeartRateMillis,_tmpEffectiveMaximumHeartRate,_tmpZone1LowerBpm,_tmpZone2LowerBpm,_tmpZone3LowerBpm,_tmpZone4LowerBpm,_tmpZone5LowerBpm,_tmpSmoothingWindowSeconds,_tmpGpsAvailable,_tmpHeartRateAvailable,_tmpEndReason,_tmpLastRouteSequence,_tmpLastCheckpointEpochMillis,_tmpRecoveredPartial)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInProgressSession(): WorkoutSessionEntity? {
    val _sql: String = "SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY startEpochMillis DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfStartEpochMillis: Int = getColumnIndexOrThrow(_stmt, "startEpochMillis")
        val _columnIndexOfEndEpochMillis: Int = getColumnIndexOrThrow(_stmt, "endEpochMillis")
        val _columnIndexOfStartMonotonicMillis: Int = getColumnIndexOrThrow(_stmt, "startMonotonicMillis")
        val _columnIndexOfElapsedMillis: Int = getColumnIndexOrThrow(_stmt, "elapsedMillis")
        val _columnIndexOfActiveMillis: Int = getColumnIndexOrThrow(_stmt, "activeMillis")
        val _columnIndexOfDistanceMetres: Int = getColumnIndexOrThrow(_stmt, "distanceMetres")
        val _columnIndexOfDistanceSource: Int = getColumnIndexOrThrow(_stmt, "distanceSource")
        val _columnIndexOfAveragePaceMillisPerKm: Int = getColumnIndexOrThrow(_stmt, "averagePaceMillisPerKm")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "maximumHeartRate")
        val _columnIndexOfHeartRateSampleSum: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleSum")
        val _columnIndexOfHeartRateSampleCount: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleCount")
        val _columnIndexOfZone1Millis: Int = getColumnIndexOrThrow(_stmt, "zone1Millis")
        val _columnIndexOfZone2Millis: Int = getColumnIndexOrThrow(_stmt, "zone2Millis")
        val _columnIndexOfZone3Millis: Int = getColumnIndexOrThrow(_stmt, "zone3Millis")
        val _columnIndexOfZone4Millis: Int = getColumnIndexOrThrow(_stmt, "zone4Millis")
        val _columnIndexOfZone5Millis: Int = getColumnIndexOrThrow(_stmt, "zone5Millis")
        val _columnIndexOfUnclassifiedHeartRateMillis: Int = getColumnIndexOrThrow(_stmt, "unclassifiedHeartRateMillis")
        val _columnIndexOfEffectiveMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "effectiveMaximumHeartRate")
        val _columnIndexOfZone1LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone1LowerBpm")
        val _columnIndexOfZone2LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone2LowerBpm")
        val _columnIndexOfZone3LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone3LowerBpm")
        val _columnIndexOfZone4LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone4LowerBpm")
        val _columnIndexOfZone5LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone5LowerBpm")
        val _columnIndexOfSmoothingWindowSeconds: Int = getColumnIndexOrThrow(_stmt, "smoothingWindowSeconds")
        val _columnIndexOfGpsAvailable: Int = getColumnIndexOrThrow(_stmt, "gpsAvailable")
        val _columnIndexOfHeartRateAvailable: Int = getColumnIndexOrThrow(_stmt, "heartRateAvailable")
        val _columnIndexOfEndReason: Int = getColumnIndexOrThrow(_stmt, "endReason")
        val _columnIndexOfLastRouteSequence: Int = getColumnIndexOrThrow(_stmt, "lastRouteSequence")
        val _columnIndexOfLastCheckpointEpochMillis: Int = getColumnIndexOrThrow(_stmt, "lastCheckpointEpochMillis")
        val _columnIndexOfRecoveredPartial: Int = getColumnIndexOrThrow(_stmt, "recoveredPartial")
        val _result: WorkoutSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpStatus: SessionStatus
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __databaseConverters.sessionStatus(_tmp)
          val _tmpStartEpochMillis: Long
          _tmpStartEpochMillis = _stmt.getLong(_columnIndexOfStartEpochMillis)
          val _tmpEndEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMillis)) {
            _tmpEndEpochMillis = null
          } else {
            _tmpEndEpochMillis = _stmt.getLong(_columnIndexOfEndEpochMillis)
          }
          val _tmpStartMonotonicMillis: Long
          _tmpStartMonotonicMillis = _stmt.getLong(_columnIndexOfStartMonotonicMillis)
          val _tmpElapsedMillis: Long
          _tmpElapsedMillis = _stmt.getLong(_columnIndexOfElapsedMillis)
          val _tmpActiveMillis: Long
          _tmpActiveMillis = _stmt.getLong(_columnIndexOfActiveMillis)
          val _tmpDistanceMetres: Double
          _tmpDistanceMetres = _stmt.getDouble(_columnIndexOfDistanceMetres)
          val _tmpDistanceSource: DistanceSource
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfDistanceSource)
          _tmpDistanceSource = __databaseConverters.distanceSource(_tmp_1)
          val _tmpAveragePaceMillisPerKm: Long?
          if (_stmt.isNull(_columnIndexOfAveragePaceMillisPerKm)) {
            _tmpAveragePaceMillisPerKm = null
          } else {
            _tmpAveragePaceMillisPerKm = _stmt.getLong(_columnIndexOfAveragePaceMillisPerKm)
          }
          val _tmpAverageHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getDouble(_columnIndexOfAverageHeartRate)
          }
          val _tmpMaximumHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfMaximumHeartRate)) {
            _tmpMaximumHeartRate = null
          } else {
            _tmpMaximumHeartRate = _stmt.getDouble(_columnIndexOfMaximumHeartRate)
          }
          val _tmpHeartRateSampleSum: Double
          _tmpHeartRateSampleSum = _stmt.getDouble(_columnIndexOfHeartRateSampleSum)
          val _tmpHeartRateSampleCount: Long
          _tmpHeartRateSampleCount = _stmt.getLong(_columnIndexOfHeartRateSampleCount)
          val _tmpZone1Millis: Long
          _tmpZone1Millis = _stmt.getLong(_columnIndexOfZone1Millis)
          val _tmpZone2Millis: Long
          _tmpZone2Millis = _stmt.getLong(_columnIndexOfZone2Millis)
          val _tmpZone3Millis: Long
          _tmpZone3Millis = _stmt.getLong(_columnIndexOfZone3Millis)
          val _tmpZone4Millis: Long
          _tmpZone4Millis = _stmt.getLong(_columnIndexOfZone4Millis)
          val _tmpZone5Millis: Long
          _tmpZone5Millis = _stmt.getLong(_columnIndexOfZone5Millis)
          val _tmpUnclassifiedHeartRateMillis: Long
          _tmpUnclassifiedHeartRateMillis = _stmt.getLong(_columnIndexOfUnclassifiedHeartRateMillis)
          val _tmpEffectiveMaximumHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfEffectiveMaximumHeartRate)) {
            _tmpEffectiveMaximumHeartRate = null
          } else {
            _tmpEffectiveMaximumHeartRate = _stmt.getLong(_columnIndexOfEffectiveMaximumHeartRate).toInt()
          }
          val _tmpZone1LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone1LowerBpm)) {
            _tmpZone1LowerBpm = null
          } else {
            _tmpZone1LowerBpm = _stmt.getLong(_columnIndexOfZone1LowerBpm).toInt()
          }
          val _tmpZone2LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone2LowerBpm)) {
            _tmpZone2LowerBpm = null
          } else {
            _tmpZone2LowerBpm = _stmt.getLong(_columnIndexOfZone2LowerBpm).toInt()
          }
          val _tmpZone3LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone3LowerBpm)) {
            _tmpZone3LowerBpm = null
          } else {
            _tmpZone3LowerBpm = _stmt.getLong(_columnIndexOfZone3LowerBpm).toInt()
          }
          val _tmpZone4LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone4LowerBpm)) {
            _tmpZone4LowerBpm = null
          } else {
            _tmpZone4LowerBpm = _stmt.getLong(_columnIndexOfZone4LowerBpm).toInt()
          }
          val _tmpZone5LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone5LowerBpm)) {
            _tmpZone5LowerBpm = null
          } else {
            _tmpZone5LowerBpm = _stmt.getLong(_columnIndexOfZone5LowerBpm).toInt()
          }
          val _tmpSmoothingWindowSeconds: Int
          _tmpSmoothingWindowSeconds = _stmt.getLong(_columnIndexOfSmoothingWindowSeconds).toInt()
          val _tmpGpsAvailable: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfGpsAvailable).toInt()
          _tmpGpsAvailable = _tmp_2 != 0
          val _tmpHeartRateAvailable: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfHeartRateAvailable).toInt()
          _tmpHeartRateAvailable = _tmp_3 != 0
          val _tmpEndReason: Int?
          if (_stmt.isNull(_columnIndexOfEndReason)) {
            _tmpEndReason = null
          } else {
            _tmpEndReason = _stmt.getLong(_columnIndexOfEndReason).toInt()
          }
          val _tmpLastRouteSequence: Long
          _tmpLastRouteSequence = _stmt.getLong(_columnIndexOfLastRouteSequence)
          val _tmpLastCheckpointEpochMillis: Long
          _tmpLastCheckpointEpochMillis = _stmt.getLong(_columnIndexOfLastCheckpointEpochMillis)
          val _tmpRecoveredPartial: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfRecoveredPartial).toInt()
          _tmpRecoveredPartial = _tmp_4 != 0
          _result = WorkoutSessionEntity(_tmpId,_tmpStatus,_tmpStartEpochMillis,_tmpEndEpochMillis,_tmpStartMonotonicMillis,_tmpElapsedMillis,_tmpActiveMillis,_tmpDistanceMetres,_tmpDistanceSource,_tmpAveragePaceMillisPerKm,_tmpAverageHeartRate,_tmpMaximumHeartRate,_tmpHeartRateSampleSum,_tmpHeartRateSampleCount,_tmpZone1Millis,_tmpZone2Millis,_tmpZone3Millis,_tmpZone4Millis,_tmpZone5Millis,_tmpUnclassifiedHeartRateMillis,_tmpEffectiveMaximumHeartRate,_tmpZone1LowerBpm,_tmpZone2LowerBpm,_tmpZone3LowerBpm,_tmpZone4LowerBpm,_tmpZone5LowerBpm,_tmpSmoothingWindowSeconds,_tmpGpsAvailable,_tmpHeartRateAvailable,_tmpEndReason,_tmpLastRouteSequence,_tmpLastCheckpointEpochMillis,_tmpRecoveredPartial)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun recentSessions(limit: Int): Flow<List<WorkoutSessionEntity>> {
    val _sql: String = "SELECT * FROM workout_sessions WHERE status != 'IN_PROGRESS' ORDER BY startEpochMillis DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("workout_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfStartEpochMillis: Int = getColumnIndexOrThrow(_stmt, "startEpochMillis")
        val _columnIndexOfEndEpochMillis: Int = getColumnIndexOrThrow(_stmt, "endEpochMillis")
        val _columnIndexOfStartMonotonicMillis: Int = getColumnIndexOrThrow(_stmt, "startMonotonicMillis")
        val _columnIndexOfElapsedMillis: Int = getColumnIndexOrThrow(_stmt, "elapsedMillis")
        val _columnIndexOfActiveMillis: Int = getColumnIndexOrThrow(_stmt, "activeMillis")
        val _columnIndexOfDistanceMetres: Int = getColumnIndexOrThrow(_stmt, "distanceMetres")
        val _columnIndexOfDistanceSource: Int = getColumnIndexOrThrow(_stmt, "distanceSource")
        val _columnIndexOfAveragePaceMillisPerKm: Int = getColumnIndexOrThrow(_stmt, "averagePaceMillisPerKm")
        val _columnIndexOfAverageHeartRate: Int = getColumnIndexOrThrow(_stmt, "averageHeartRate")
        val _columnIndexOfMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "maximumHeartRate")
        val _columnIndexOfHeartRateSampleSum: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleSum")
        val _columnIndexOfHeartRateSampleCount: Int = getColumnIndexOrThrow(_stmt, "heartRateSampleCount")
        val _columnIndexOfZone1Millis: Int = getColumnIndexOrThrow(_stmt, "zone1Millis")
        val _columnIndexOfZone2Millis: Int = getColumnIndexOrThrow(_stmt, "zone2Millis")
        val _columnIndexOfZone3Millis: Int = getColumnIndexOrThrow(_stmt, "zone3Millis")
        val _columnIndexOfZone4Millis: Int = getColumnIndexOrThrow(_stmt, "zone4Millis")
        val _columnIndexOfZone5Millis: Int = getColumnIndexOrThrow(_stmt, "zone5Millis")
        val _columnIndexOfUnclassifiedHeartRateMillis: Int = getColumnIndexOrThrow(_stmt, "unclassifiedHeartRateMillis")
        val _columnIndexOfEffectiveMaximumHeartRate: Int = getColumnIndexOrThrow(_stmt, "effectiveMaximumHeartRate")
        val _columnIndexOfZone1LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone1LowerBpm")
        val _columnIndexOfZone2LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone2LowerBpm")
        val _columnIndexOfZone3LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone3LowerBpm")
        val _columnIndexOfZone4LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone4LowerBpm")
        val _columnIndexOfZone5LowerBpm: Int = getColumnIndexOrThrow(_stmt, "zone5LowerBpm")
        val _columnIndexOfSmoothingWindowSeconds: Int = getColumnIndexOrThrow(_stmt, "smoothingWindowSeconds")
        val _columnIndexOfGpsAvailable: Int = getColumnIndexOrThrow(_stmt, "gpsAvailable")
        val _columnIndexOfHeartRateAvailable: Int = getColumnIndexOrThrow(_stmt, "heartRateAvailable")
        val _columnIndexOfEndReason: Int = getColumnIndexOrThrow(_stmt, "endReason")
        val _columnIndexOfLastRouteSequence: Int = getColumnIndexOrThrow(_stmt, "lastRouteSequence")
        val _columnIndexOfLastCheckpointEpochMillis: Int = getColumnIndexOrThrow(_stmt, "lastCheckpointEpochMillis")
        val _columnIndexOfRecoveredPartial: Int = getColumnIndexOrThrow(_stmt, "recoveredPartial")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpStatus: SessionStatus
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __databaseConverters.sessionStatus(_tmp)
          val _tmpStartEpochMillis: Long
          _tmpStartEpochMillis = _stmt.getLong(_columnIndexOfStartEpochMillis)
          val _tmpEndEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMillis)) {
            _tmpEndEpochMillis = null
          } else {
            _tmpEndEpochMillis = _stmt.getLong(_columnIndexOfEndEpochMillis)
          }
          val _tmpStartMonotonicMillis: Long
          _tmpStartMonotonicMillis = _stmt.getLong(_columnIndexOfStartMonotonicMillis)
          val _tmpElapsedMillis: Long
          _tmpElapsedMillis = _stmt.getLong(_columnIndexOfElapsedMillis)
          val _tmpActiveMillis: Long
          _tmpActiveMillis = _stmt.getLong(_columnIndexOfActiveMillis)
          val _tmpDistanceMetres: Double
          _tmpDistanceMetres = _stmt.getDouble(_columnIndexOfDistanceMetres)
          val _tmpDistanceSource: DistanceSource
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfDistanceSource)
          _tmpDistanceSource = __databaseConverters.distanceSource(_tmp_1)
          val _tmpAveragePaceMillisPerKm: Long?
          if (_stmt.isNull(_columnIndexOfAveragePaceMillisPerKm)) {
            _tmpAveragePaceMillisPerKm = null
          } else {
            _tmpAveragePaceMillisPerKm = _stmt.getLong(_columnIndexOfAveragePaceMillisPerKm)
          }
          val _tmpAverageHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfAverageHeartRate)) {
            _tmpAverageHeartRate = null
          } else {
            _tmpAverageHeartRate = _stmt.getDouble(_columnIndexOfAverageHeartRate)
          }
          val _tmpMaximumHeartRate: Double?
          if (_stmt.isNull(_columnIndexOfMaximumHeartRate)) {
            _tmpMaximumHeartRate = null
          } else {
            _tmpMaximumHeartRate = _stmt.getDouble(_columnIndexOfMaximumHeartRate)
          }
          val _tmpHeartRateSampleSum: Double
          _tmpHeartRateSampleSum = _stmt.getDouble(_columnIndexOfHeartRateSampleSum)
          val _tmpHeartRateSampleCount: Long
          _tmpHeartRateSampleCount = _stmt.getLong(_columnIndexOfHeartRateSampleCount)
          val _tmpZone1Millis: Long
          _tmpZone1Millis = _stmt.getLong(_columnIndexOfZone1Millis)
          val _tmpZone2Millis: Long
          _tmpZone2Millis = _stmt.getLong(_columnIndexOfZone2Millis)
          val _tmpZone3Millis: Long
          _tmpZone3Millis = _stmt.getLong(_columnIndexOfZone3Millis)
          val _tmpZone4Millis: Long
          _tmpZone4Millis = _stmt.getLong(_columnIndexOfZone4Millis)
          val _tmpZone5Millis: Long
          _tmpZone5Millis = _stmt.getLong(_columnIndexOfZone5Millis)
          val _tmpUnclassifiedHeartRateMillis: Long
          _tmpUnclassifiedHeartRateMillis = _stmt.getLong(_columnIndexOfUnclassifiedHeartRateMillis)
          val _tmpEffectiveMaximumHeartRate: Int?
          if (_stmt.isNull(_columnIndexOfEffectiveMaximumHeartRate)) {
            _tmpEffectiveMaximumHeartRate = null
          } else {
            _tmpEffectiveMaximumHeartRate = _stmt.getLong(_columnIndexOfEffectiveMaximumHeartRate).toInt()
          }
          val _tmpZone1LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone1LowerBpm)) {
            _tmpZone1LowerBpm = null
          } else {
            _tmpZone1LowerBpm = _stmt.getLong(_columnIndexOfZone1LowerBpm).toInt()
          }
          val _tmpZone2LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone2LowerBpm)) {
            _tmpZone2LowerBpm = null
          } else {
            _tmpZone2LowerBpm = _stmt.getLong(_columnIndexOfZone2LowerBpm).toInt()
          }
          val _tmpZone3LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone3LowerBpm)) {
            _tmpZone3LowerBpm = null
          } else {
            _tmpZone3LowerBpm = _stmt.getLong(_columnIndexOfZone3LowerBpm).toInt()
          }
          val _tmpZone4LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone4LowerBpm)) {
            _tmpZone4LowerBpm = null
          } else {
            _tmpZone4LowerBpm = _stmt.getLong(_columnIndexOfZone4LowerBpm).toInt()
          }
          val _tmpZone5LowerBpm: Int?
          if (_stmt.isNull(_columnIndexOfZone5LowerBpm)) {
            _tmpZone5LowerBpm = null
          } else {
            _tmpZone5LowerBpm = _stmt.getLong(_columnIndexOfZone5LowerBpm).toInt()
          }
          val _tmpSmoothingWindowSeconds: Int
          _tmpSmoothingWindowSeconds = _stmt.getLong(_columnIndexOfSmoothingWindowSeconds).toInt()
          val _tmpGpsAvailable: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfGpsAvailable).toInt()
          _tmpGpsAvailable = _tmp_2 != 0
          val _tmpHeartRateAvailable: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfHeartRateAvailable).toInt()
          _tmpHeartRateAvailable = _tmp_3 != 0
          val _tmpEndReason: Int?
          if (_stmt.isNull(_columnIndexOfEndReason)) {
            _tmpEndReason = null
          } else {
            _tmpEndReason = _stmt.getLong(_columnIndexOfEndReason).toInt()
          }
          val _tmpLastRouteSequence: Long
          _tmpLastRouteSequence = _stmt.getLong(_columnIndexOfLastRouteSequence)
          val _tmpLastCheckpointEpochMillis: Long
          _tmpLastCheckpointEpochMillis = _stmt.getLong(_columnIndexOfLastCheckpointEpochMillis)
          val _tmpRecoveredPartial: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfRecoveredPartial).toInt()
          _tmpRecoveredPartial = _tmp_4 != 0
          _item = WorkoutSessionEntity(_tmpId,_tmpStatus,_tmpStartEpochMillis,_tmpEndEpochMillis,_tmpStartMonotonicMillis,_tmpElapsedMillis,_tmpActiveMillis,_tmpDistanceMetres,_tmpDistanceSource,_tmpAveragePaceMillisPerKm,_tmpAverageHeartRate,_tmpMaximumHeartRate,_tmpHeartRateSampleSum,_tmpHeartRateSampleCount,_tmpZone1Millis,_tmpZone2Millis,_tmpZone3Millis,_tmpZone4Millis,_tmpZone5Millis,_tmpUnclassifiedHeartRateMillis,_tmpEffectiveMaximumHeartRate,_tmpZone1LowerBpm,_tmpZone2LowerBpm,_tmpZone3LowerBpm,_tmpZone4LowerBpm,_tmpZone5LowerBpm,_tmpSmoothingWindowSeconds,_tmpGpsAvailable,_tmpHeartRateAvailable,_tmpEndReason,_tmpLastRouteSequence,_tmpLastCheckpointEpochMillis,_tmpRecoveredPartial)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun pauses(sessionId: String): List<PausePeriodEntity> {
    val _sql: String = "SELECT * FROM pause_periods WHERE sessionId = ? ORDER BY sequence"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfSequence: Int = getColumnIndexOrThrow(_stmt, "sequence")
        val _columnIndexOfStartEpochMillis: Int = getColumnIndexOrThrow(_stmt, "startEpochMillis")
        val _columnIndexOfEndEpochMillis: Int = getColumnIndexOrThrow(_stmt, "endEpochMillis")
        val _columnIndexOfStartActiveMillis: Int = getColumnIndexOrThrow(_stmt, "startActiveMillis")
        val _columnIndexOfEndActiveMillis: Int = getColumnIndexOrThrow(_stmt, "endActiveMillis")
        val _columnIndexOfReason: Int = getColumnIndexOrThrow(_stmt, "reason")
        val _result: MutableList<PausePeriodEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PausePeriodEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpSequence: Long
          _tmpSequence = _stmt.getLong(_columnIndexOfSequence)
          val _tmpStartEpochMillis: Long
          _tmpStartEpochMillis = _stmt.getLong(_columnIndexOfStartEpochMillis)
          val _tmpEndEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMillis)) {
            _tmpEndEpochMillis = null
          } else {
            _tmpEndEpochMillis = _stmt.getLong(_columnIndexOfEndEpochMillis)
          }
          val _tmpStartActiveMillis: Long
          _tmpStartActiveMillis = _stmt.getLong(_columnIndexOfStartActiveMillis)
          val _tmpEndActiveMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndActiveMillis)) {
            _tmpEndActiveMillis = null
          } else {
            _tmpEndActiveMillis = _stmt.getLong(_columnIndexOfEndActiveMillis)
          }
          val _tmpReason: PauseReason
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfReason)
          _tmpReason = __databaseConverters.pauseReason(_tmp)
          _item = PausePeriodEntity(_tmpSessionId,_tmpSequence,_tmpStartEpochMillis,_tmpEndEpochMillis,_tmpStartActiveMillis,_tmpEndActiveMillis,_tmpReason)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun routePoints(sessionId: String): List<RoutePointEntity> {
    val _sql: String = "SELECT * FROM route_points WHERE sessionId = ? ORDER BY sequence"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfSequence: Int = getColumnIndexOrThrow(_stmt, "sequence")
        val _columnIndexOfEpochMillis: Int = getColumnIndexOrThrow(_stmt, "epochMillis")
        val _columnIndexOfActiveOffsetMillis: Int = getColumnIndexOrThrow(_stmt, "activeOffsetMillis")
        val _columnIndexOfLatitude: Int = getColumnIndexOrThrow(_stmt, "latitude")
        val _columnIndexOfLongitude: Int = getColumnIndexOrThrow(_stmt, "longitude")
        val _columnIndexOfAltitudeMetres: Int = getColumnIndexOrThrow(_stmt, "altitudeMetres")
        val _columnIndexOfBearingDegrees: Int = getColumnIndexOrThrow(_stmt, "bearingDegrees")
        val _columnIndexOfHorizontalAccuracyMetres: Int = getColumnIndexOrThrow(_stmt, "horizontalAccuracyMetres")
        val _columnIndexOfContinuitySegment: Int = getColumnIndexOrThrow(_stmt, "continuitySegment")
        val _result: MutableList<RoutePointEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RoutePointEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpSequence: Long
          _tmpSequence = _stmt.getLong(_columnIndexOfSequence)
          val _tmpEpochMillis: Long
          _tmpEpochMillis = _stmt.getLong(_columnIndexOfEpochMillis)
          val _tmpActiveOffsetMillis: Long
          _tmpActiveOffsetMillis = _stmt.getLong(_columnIndexOfActiveOffsetMillis)
          val _tmpLatitude: Double
          _tmpLatitude = _stmt.getDouble(_columnIndexOfLatitude)
          val _tmpLongitude: Double
          _tmpLongitude = _stmt.getDouble(_columnIndexOfLongitude)
          val _tmpAltitudeMetres: Double?
          if (_stmt.isNull(_columnIndexOfAltitudeMetres)) {
            _tmpAltitudeMetres = null
          } else {
            _tmpAltitudeMetres = _stmt.getDouble(_columnIndexOfAltitudeMetres)
          }
          val _tmpBearingDegrees: Double?
          if (_stmt.isNull(_columnIndexOfBearingDegrees)) {
            _tmpBearingDegrees = null
          } else {
            _tmpBearingDegrees = _stmt.getDouble(_columnIndexOfBearingDegrees)
          }
          val _tmpHorizontalAccuracyMetres: Double
          _tmpHorizontalAccuracyMetres = _stmt.getDouble(_columnIndexOfHorizontalAccuracyMetres)
          val _tmpContinuitySegment: Int
          _tmpContinuitySegment = _stmt.getLong(_columnIndexOfContinuitySegment).toInt()
          _item = RoutePointEntity(_tmpSessionId,_tmpSequence,_tmpEpochMillis,_tmpActiveOffsetMillis,_tmpLatitude,_tmpLongitude,_tmpAltitudeMetres,_tmpBearingDegrees,_tmpHorizontalAccuracyMetres,_tmpContinuitySegment)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
