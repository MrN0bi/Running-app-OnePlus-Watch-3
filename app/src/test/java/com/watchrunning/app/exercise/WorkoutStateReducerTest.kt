package com.watchrunning.app.exercise

import com.watchrunning.app.model.WorkoutCommand
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class WorkoutStateReducerTest {
    @Test fun onlyLegalCommandsBecomePending() {
        val active = WorkoutUiState(phase = WorkoutPhase.Active)
        assertSame(WorkoutCommand.Pause, WorkoutStateReducer.pending(active, WorkoutCommand.Pause)?.pendingCommand)
        assertNull(WorkoutStateReducer.pending(active, WorkoutCommand.Resume))
    }

    @Test fun duplicateCommandIsIgnoredWhilePending() {
        val active = WorkoutUiState(phase = WorkoutPhase.Active, pendingCommand = WorkoutCommand.Pause)
        assertNull(WorkoutStateReducer.pending(active, WorkoutCommand.Pause))
    }

    @Test fun healthServicesConfirmationClearsPendingCommand() {
        val pausing = WorkoutUiState(phase = WorkoutPhase.Active, pendingCommand = WorkoutCommand.Pause)
        val confirmed = WorkoutStateReducer.confirmed(pausing, WorkoutPhase.Paused)
        assertEquals(WorkoutPhase.Paused, confirmed.phase)
        assertNull(confirmed.pendingCommand)
    }
}
