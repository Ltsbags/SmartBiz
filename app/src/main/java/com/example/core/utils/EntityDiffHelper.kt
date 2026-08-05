package com.example.core.utils

import org.json.JSONArray
import org.json.JSONObject

object EntityDiffHelper {

    data class DiffResult(
        val oldValueJson: String?,
        val newValueJson: String?,
        val modifiedFields: List<String>,
        val modifiedFieldsJson: String
    )

    fun createSnapshotJson(obj: Any?): String? {
        if (obj == null) return null
        return try {
            when (obj) {
                is String -> obj
                is JSONObject -> obj.toString()
                else -> {
                    // Simple property map or toString json construction using reflection or field map
                    val map = objectToMap(obj)
                    JSONObject(map).toString()
                }
            }
        } catch (e: Exception) {
            obj.toString()
        }
    }

    fun compareObjects(oldObj: Any?, newObj: Any?): DiffResult {
        if (oldObj == null && newObj == null) {
            return DiffResult(null, null, emptyList(), "[]")
        }

        val oldMap = if (oldObj != null) objectToMap(oldObj) else emptyMap()
        val newMap = if (newObj != null) objectToMap(newObj) else emptyMap()

        val modifiedFields = mutableListOf<String>()
        val allKeys = oldMap.keys + newMap.keys

        for (key in allKeys) {
            val oldVal = oldMap[key]
            val newVal = newMap[key]

            if (oldVal != newVal) {
                modifiedFields.add(key)
            }
        }

        val oldJson = if (oldMap.isNotEmpty()) JSONObject(oldMap).toString() else createSnapshotJson(oldObj)
        val newJson = if (newMap.isNotEmpty()) JSONObject(newMap).toString() else createSnapshotJson(newObj)

        val jsonArray = JSONArray()
        modifiedFields.forEach { jsonArray.put(it) }

        return DiffResult(
            oldValueJson = oldJson,
            newValueJson = newJson,
            modifiedFields = modifiedFields,
            modifiedFieldsJson = jsonArray.toString()
        )
    }

    private fun objectToMap(obj: Any): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val fields = obj::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                val name = field.name
                if (name != "\$stable" && name != "Companion") {
                    val value = field.get(obj)
                    map[name] = value?.toString()
                }
            } catch (_: Exception) {
            }
        }
        return map
    }
}
