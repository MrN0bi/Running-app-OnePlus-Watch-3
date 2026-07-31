package com.watchrunning.app.calculation

import com.watchrunning.app.model.GpsSource

/** Selects the most accurate fresh location stream while avoiding one-fix source oscillation. */
class LocationSourceSelector(
    private val freshnessMillis: Long = 5_000L,
    private val minimumAbsoluteImprovementMetres: Double = 3.0,
    private val minimumRelativeImprovement: Double = 0.20,
    private val requiredChallengerFixes: Int = 2,
) {
    data class Decision(
        val useSample: Boolean,
        val source: GpsSource,
        val previousSource: GpsSource?,
        val sourceChanged: Boolean,
        internal val previousAccuracyMetres: Double?,
        internal val previousArrivalMillis: Long?,
    )

    private data class Observation(val accuracyMetres: Double, val arrivalMillis: Long)

    private val observations = mutableMapOf<GpsSource, Observation>()
    private var selectedSource: GpsSource? = null
    private var challengerSource: GpsSource? = null
    private var challengerFixCount = 0

    fun observe(source: GpsSource, accuracyMetres: Double, arrivalMillis: Long): Decision {
        require(source != GpsSource.NONE)
        require(accuracyMetres.isFinite() && accuracyMetres >= 0.0)
        val previousObservation = observations.put(source, Observation(accuracyMetres, arrivalMillis))

        val current = selectedSource
        if (current == null) return switchTo(source, previousObservation)
        if (current == source) {
            return decision(true, source, current, sourceChanged = false, previousObservation)
        }

        val currentObservation = observations[current]
        if (currentObservation == null || arrivalMillis - currentObservation.arrivalMillis > freshnessMillis) {
            return switchTo(source, previousObservation)
        }

        val absoluteImprovement = currentObservation.accuracyMetres - accuracyMetres
        val relativeImprovement = if (currentObservation.accuracyMetres > 0.0) {
            absoluteImprovement / currentObservation.accuracyMetres
        } else {
            0.0
        }
        val meaningfullyBetter = absoluteImprovement >= minimumAbsoluteImprovementMetres ||
            relativeImprovement >= minimumRelativeImprovement
        if (!meaningfullyBetter) {
            if (challengerSource == source) clearChallenger()
            return decision(false, source, current, sourceChanged = false, previousObservation)
        }

        if (challengerSource == source) {
            challengerFixCount++
        } else {
            challengerSource = source
            challengerFixCount = 1
        }
        return if (challengerFixCount >= requiredChallengerFixes) {
            switchTo(source, previousObservation)
        } else {
            decision(false, source, current, sourceChanged = false, previousObservation)
        }
    }

    fun selectedSource(nowMillis: Long): GpsSource? {
        val source = selectedSource ?: return null
        val observation = observations[source] ?: return null
        return source.takeIf { nowMillis - observation.arrivalMillis <= freshnessMillis }
    }

    fun reject(decision: Decision) {
        if (decision.previousAccuracyMetres != null && decision.previousArrivalMillis != null) {
            observations[decision.source] = Observation(
                decision.previousAccuracyMetres,
                decision.previousArrivalMillis,
            )
        } else {
            observations.remove(decision.source)
        }
        if (decision.sourceChanged && selectedSource == decision.source) {
            selectedSource = decision.previousSource
        }
        clearChallenger()
    }

    fun reset() {
        observations.clear()
        selectedSource = null
        clearChallenger()
    }

    private fun switchTo(source: GpsSource, previousObservation: Observation?): Decision {
        val previous = selectedSource
        selectedSource = source
        clearChallenger()
        return decision(true, source, previous, sourceChanged = previous != source, previousObservation)
    }

    private fun decision(
        useSample: Boolean,
        source: GpsSource,
        previousSource: GpsSource?,
        sourceChanged: Boolean,
        previousObservation: Observation?,
    ) = Decision(
        useSample = useSample,
        source = source,
        previousSource = previousSource,
        sourceChanged = sourceChanged,
        previousAccuracyMetres = previousObservation?.accuracyMetres,
        previousArrivalMillis = previousObservation?.arrivalMillis,
    )

    private fun clearChallenger() {
        challengerSource = null
        challengerFixCount = 0
    }
}
