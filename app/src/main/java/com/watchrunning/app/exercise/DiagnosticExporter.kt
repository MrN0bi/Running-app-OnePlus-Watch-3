package com.watchrunning.app.exercise

import android.content.Context
import com.watchrunning.app.data.database.WorkoutDao
import com.watchrunning.app.data.database.WorkoutSessionEntity
import java.io.File
import java.util.Locale

class DiagnosticExporter(
    private val context: Context,
    private val dao: WorkoutDao,
) {
    suspend fun export(session: WorkoutSessionEntity, capabilities: String, rejectedLocations: Long) {
        val directory = File(context.filesDir, "diagnostics").apply { mkdirs() }
        val points = dao.routePoints(session.id)
        File(directory, "latest-run.csv").bufferedWriter().use { writer ->
            writer.appendLine(
                "sequence,epoch_millis,active_millis,latitude,longitude,altitude_m,bearing_deg,horizontal_accuracy_m,segment",
            )
            points.forEach { point ->
                writer.appendLine(
                    String.format(
                        Locale.ROOT,
                        "%d,%d,%d,%.8f,%.8f,%s,%s,%.2f,%d",
                        point.sequence,
                        point.epochMillis,
                        point.activeOffsetMillis,
                        point.latitude,
                        point.longitude,
                        point.altitudeMetres?.toString().orEmpty(),
                        point.bearingDegrees?.toString().orEmpty(),
                        point.horizontalAccuracyMetres,
                        point.continuitySegment,
                    ),
                )
            }
        }
        File(directory, "latest-report.txt").writeText(
            buildString {
                appendLine("session=${session.id}")
                appendLine("status=${session.status}")
                appendLine("elapsed_ms=${session.elapsedMillis}")
                appendLine("active_ms=${session.activeMillis}")
                appendLine("distance_m=${session.distanceMetres}")
                appendLine("distance_source=${session.distanceSource}")
                appendLine("average_hr=${session.averageHeartRate}")
                appendLine("maximum_hr=${session.maximumHeartRate}")
                appendLine("rejected_locations=$rejectedLocations")
                appendLine("capabilities=$capabilities")
            },
        )
    }
}
