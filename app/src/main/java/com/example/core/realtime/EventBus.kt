package com.example.core.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import java.util.concurrent.ConcurrentHashMap

class EventBus private constructor() {

    private val _events = MutableSharedFlow<RealtimeEvent>(
        replay = 20,
        extraBufferCapacity = 100
    )
    val events: SharedFlow<RealtimeEvent> = _events.asSharedFlow()

    private val moduleSubscriptions = ConcurrentHashMap<String, MutableList<(RealtimeEvent) -> Unit>>()

    suspend fun publish(event: RealtimeEvent) {
        _events.emit(event)
        moduleSubscriptions[event.module]?.forEach { listener ->
            try {
                listener(event)
            } catch (_: Exception) {}
        }
    }

    fun publishSync(event: RealtimeEvent) {
        _events.tryEmit(event)
        moduleSubscriptions[event.module]?.forEach { listener ->
            try {
                listener(event)
            } catch (_: Exception) {}
        }
    }

    fun subscribeByModule(module: String): Flow<RealtimeEvent> {
        return events.filter { it.module.equals(module, ignoreCase = true) }
    }

    fun subscribeByEventType(eventType: String): Flow<RealtimeEvent> {
        return events.filter { it.eventType.equals(eventType, ignoreCase = true) }
    }

    inline fun <reified T : RealtimeEvent> subscribeTo(): Flow<T> {
        return events.filter { it is T }.filter { true } as Flow<T>
    }

    fun registerModuleListener(module: String, listener: (RealtimeEvent) -> Unit) {
        moduleSubscriptions.computeIfAbsent(module) { mutableListOf() }.add(listener)
    }

    fun unregisterModuleListener(module: String, listener: (RealtimeEvent) -> Unit) {
        moduleSubscriptions[module]?.remove(listener)
    }

    companion object {
        @Volatile
        private var instance: EventBus? = null

        fun getInstance(): EventBus {
            return instance ?: synchronized(this) {
                instance ?: EventBus().also { instance = it }
            }
        }
    }
}
