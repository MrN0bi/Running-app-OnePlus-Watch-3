package com.watchrunning.app.exercise

import com.watchrunning.app.model.WorkoutCommand
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState

object WorkoutStateReducer {
    fun pending(state: WorkoutUiState, command: WorkoutCommand): WorkoutUiState? {
        if (state.pendingCommand != null) return null
        val legal = when (command) {
            WorkoutCommand.Prepare -> state.phase in setOf(WorkoutPhase.Idle, WorkoutPhase.Ended, WorkoutPhase.Interrupted, WorkoutPhase.Error)
            WorkoutCommand.Start, WorkoutCommand.StartWithoutFix -> state.phase == WorkoutPhase.Preparing
            WorkoutCommand.Pause -> state.phase == WorkoutPhase.Active
            WorkoutCommand.Resume -> state.phase == WorkoutPhase.Paused
            WorkoutCommand.RequestEnd -> state.phase == WorkoutPhase.Paused
            WorkoutCommand.ConfirmEnd -> state.phase == WorkoutPhase.Paused
            WorkoutCommand.CancelPrepare -> state.phase == WorkoutPhase.Preparing
        }
        return state.takeIf { legal }?.copy(pendingCommand = command)
    }

    fun confirmed(state: WorkoutUiState, phase: WorkoutPhase): WorkoutUiState =
        state.copy(phase = phase, pendingCommand = null, error = null)

    fun failed(state: WorkoutUiState, message: String): WorkoutUiState =
        state.copy(pendingCommand = null, error = message, phase = WorkoutPhase.Error)
}
