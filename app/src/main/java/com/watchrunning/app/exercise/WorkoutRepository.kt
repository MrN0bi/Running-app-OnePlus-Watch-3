package com.watchrunning.app.exercise

import com.watchrunning.app.model.ExerciseCapabilitiesSnapshot
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkoutRepository {
    private val mutableState = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = mutableState.asStateFlow()

    private val mutableCapabilities = MutableStateFlow(ExerciseCapabilitiesSnapshot())
    val capabilities: StateFlow<ExerciseCapabilitiesSnapshot> = mutableCapabilities.asStateFlow()

    fun updateState(transform: (WorkoutUiState) -> WorkoutUiState) {
        mutableState.update(transform)
    }

    fun replaceState(state: WorkoutUiState) {
        mutableState.value = state
    }

    fun updateCapabilities(capabilities: ExerciseCapabilitiesSnapshot) {
        mutableCapabilities.value = capabilities
    }

    fun fail(message: String, interrupted: Boolean = false) {
        mutableState.update {
            it.copy(
                phase = if (interrupted) WorkoutPhase.Interrupted else WorkoutPhase.Error,
                pendingCommand = null,
                error = message,
            )
        }
    }
}
