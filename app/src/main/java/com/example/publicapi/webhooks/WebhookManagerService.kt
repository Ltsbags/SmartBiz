package com.example.publicapi.webhooks

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class WebhookSubscription(
    val id: String = UUID.randomUUID().toString(),
    val targetUrl: String,
    val events: List<String>,
    val secretKey: String,
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, DELETED
    val createdAt: Long = System.currentTimeMillis(),
    val failedAttempts: Int = 0
)

data class WebhookDeliveryLog(
    val id: String = UUID.randomUUID().toString(),
    val subscriptionId: String,
    val eventType: String,
    val targetUrl: String,
    val payloadJson: String,
    val signature: String,
    val responseStatusCode: Int,
    val isSuccessful: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class WebhookManagerService {

    private val subscriptions = ConcurrentHashMap<String, WebhookSubscription>()
    private val deliveryLogs = mutableListOf<WebhookDeliveryLog>()

    fun registerSubscription(targetUrl: String, events: List<String>): Pair<WebhookSubscription, String> {
        val secretKey = "whsec_${UUID.randomUUID().toString().replace("-", "")}"
        val subscription = WebhookSubscription(
            targetUrl = targetUrl,
            events = events,
            secretKey = secretKey
        )
        subscriptions[subscription.id] = subscription
        return Pair(subscription, secretKey)
    }

    fun getAllSubscriptions(): List<WebhookSubscription> {
        return subscriptions.values.filter { it.status != "DELETED" }
    }

    fun getSubscription(id: String): WebhookSubscription? {
        return subscriptions[id]?.takeIf { it.status != "DELETED" }
    }

    fun deleteSubscription(id: String): Boolean {
        val sub = subscriptions[id] ?: return false
        subscriptions[id] = sub.copy(status = "DELETED")
        return true
    }

    fun dispatchEvent(eventType: String, payloadJson: String): List<WebhookDeliveryLog> {
        val matchingSubs = subscriptions.values.filter { sub ->
            sub.status == "ACTIVE" && (sub.events.contains(eventType) || sub.events.contains("*"))
        }

        val logs = mutableListOf<WebhookDeliveryLog>()

        for (sub in matchingSubs) {
            val signature = calculateHmacSignature(payloadJson, sub.secretKey)
            
            // Simulated delivery execution
            val isSuccess = sub.targetUrl.startsWith("http://") || sub.targetUrl.startsWith("https://")
            val statusCode = if (isSuccess) 200 else 500
            val errorMsg = if (isSuccess) null else "Invalid URL protocol or endpoint unreachable"

            val log = WebhookDeliveryLog(
                subscriptionId = sub.id,
                eventType = eventType,
                targetUrl = sub.targetUrl,
                payloadJson = payloadJson,
                signature = signature,
                responseStatusCode = statusCode,
                isSuccessful = isSuccess,
                errorMessage = errorMsg
            )

            synchronized(deliveryLogs) {
                deliveryLogs.add(log)
            }
            logs.add(log)
        }

        return logs
    }

    fun calculateHmacSignature(data: String, secret: String): String {
        return try {
            val sha256HMAC = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
            sha256HMAC.init(secretKeySpec)
            val bytes = sha256HMAC.doFinal(data.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "sig_error_${e.message}"
        }
    }

    fun getDeliveryLogs(subscriptionId: String? = null): List<WebhookDeliveryLog> {
        synchronized(deliveryLogs) {
            return if (subscriptionId != null) {
                deliveryLogs.filter { it.subscriptionId == subscriptionId }
            } else {
                deliveryLogs.toList()
            }
        }
    }
}
