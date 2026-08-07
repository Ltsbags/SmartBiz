package com.example.core.observability

import android.util.Log
import org.json.JSONObject
import java.util.UUID

object ObservabilityLogger {

    private var currentTraceId: String = UUID.randomUUID().toString()

    fun getTraceId(): String = currentTraceId

    fun rotateTraceId(): String {
        currentTraceId = UUID.randomUUID().toString()
        return currentTraceId
    }

    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        extraData: Map<String, Any?> = emptyMap(),
        throwable: Throwable? = null
    ) {
        val jsonLog = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("level", level.name)
            put("trace_id", currentTraceId)
            put("tag", tag)
            put("message", message)
            if (extraData.isNotEmpty()) {
                val contextJson = JSONObject()
                extraData.forEach { (k, v) -> contextJson.put(k, v ?: "null") }
                put("context", contextJson)
            }
            if (throwable != null) {
                put("error_class", throwable.javaClass.simpleName)
                put("error_message", throwable.message ?: "")
                put("stack_trace", throwable.stackTraceToString())
            }
        }

        val formattedString = jsonLog.toString()

        try {
            when (level) {
                LogLevel.DEBUG -> Log.d(tag, formattedString)
                LogLevel.INFO -> Log.i(tag, formattedString)
                LogLevel.WARN -> Log.w(tag, formattedString, throwable)
                LogLevel.ERROR -> Log.e(tag, formattedString, throwable)
                LogLevel.AUDIT -> Log.i("AUDIT_$tag", formattedString)
                LogLevel.SECURITY -> Log.w("SECURITY_$tag", formattedString, throwable)
            }
        } catch (e: Throwable) {
            println("[$level] $tag: $formattedString")
        }
    }

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR, AUDIT, SECURITY
    }
}
