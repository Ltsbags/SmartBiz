package com.example.services.workflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class ExecutionQueue {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val queue = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (task in queue) {
                try {
                    task()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun enqueue(task: suspend () -> Unit) {
        queue.trySend(task)
    }
}
