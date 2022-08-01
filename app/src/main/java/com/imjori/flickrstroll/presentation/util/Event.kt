package com.imjori.flickrstroll.presentation.util

sealed class Event {
    object WalkStarted : Event()
    object WalkStopped : Event()
    object RestartLocationPollingService : Event()
}
