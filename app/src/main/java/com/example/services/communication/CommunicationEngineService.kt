package com.example.services.communication

import android.content.Context
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.repositories.CommunicationRepository
import com.example.services.communication.adapters.CommunicationChannelAdapter
import com.example.services.communication.adapters.EmailChannelAdapter
import com.example.services.communication.adapters.PushNotificationAdapter
import com.example.services.communication.adapters.SlackChannelAdapter
import com.example.services.communication.adapters.SmsChannelAdapter
import com.example.services.communication.adapters.TelegramChannelAdapter
import com.example.services.communication.adapters.WhatsAppChannelAdapter
import java.util.UUID

class CommunicationEngineService(
    private val repository: CommunicationRepository,
    val templateEngine: TemplateEngineService = TemplateEngineService(),
    val deliveryTracking: DeliveryTrackingService = DeliveryTrackingService(repository),
    val retryService: CommunicationRetryService = CommunicationRetryService(repository, deliveryTracking),
    val webhookService: WebhookFoundationService = WebhookFoundationService(deliveryTracking)
) {
    private val adapters: Map<String, CommunicationChannelAdapter> = mapOf(
        "WHATSAPP" to WhatsAppChannelAdapter(),
        "EMAIL" to EmailChannelAdapter(),
        "SMS" to SmsChannelAdapter(),
        "PUSH" to PushNotificationAdapter(),
        "TELEGRAM" to TelegramChannelAdapter(),
        "SLACK" to SlackChannelAdapter()
    )

    suspend fun sendDirectMessage(
        context: Context,
        channel: String,
        recipient: String,
        recipientName: String = "",
        subject: String = "",
        body: String,
        relatedEntityType: String = "",
        relatedEntityId: String = ""
    ): Long {
        val guid = UUID.randomUUID().toString()
        val message = CommunicationMessageEntity(
            messageGuid = guid,
            channel = channel.uppercase(),
            recipient = recipient,
            recipientName = recipientName,
            subject = subject,
            body = body,
            status = "QUEUED",
            relatedEntityType = relatedEntityType,
            relatedEntityId = relatedEntityId
        )

        val messageId = repository.saveMessage(message)
        deliveryTracking.recordStatusChange(
            messageId,
            DeliveryStatus.QUEUED,
            "Queued for dispatch via ${channel.uppercase()}"
        )

        dispatchMessage(context, message.copy(id = messageId))
        return messageId
    }

    suspend fun sendTemplatedMessage(
        context: Context,
        templateId: String,
        channel: String? = null,
        recipient: String,
        recipientName: String = "",
        variables: Map<String, String>,
        relatedEntityType: String = "",
        relatedEntityId: String = ""
    ): Long? {
        val template = repository.getTemplateById(templateId) ?: return null
        val targetChannel = (channel ?: template.channel).uppercase()
        val rendered = templateEngine.renderTemplate(template, variables)

        val finalBody = if (targetChannel == "WHATSAPP") {
            templateEngine.formatForWhatsAppMarkdown(rendered.body)
        } else {
            rendered.body
        }

        return sendDirectMessage(
            context = context,
            channel = targetChannel,
            recipient = recipient,
            recipientName = recipientName,
            subject = rendered.subject,
            body = finalBody,
            relatedEntityType = relatedEntityType.ifBlank { "TEMPLATED" },
            relatedEntityId = relatedEntityId
        )
    }

    suspend fun handleBusinessDomainEvent(
        context: Context,
        eventType: String,
        recipient: String,
        recipientName: String,
        variables: Map<String, String>,
        relatedEntityType: String = "",
        relatedEntityId: String = ""
    ) {
        val rules = repository.getActiveRulesForEvent(eventType)
        for (rule in rules) {
            sendTemplatedMessage(
                context = context,
                templateId = rule.templateId,
                channel = rule.targetChannel,
                recipient = recipient,
                recipientName = recipientName,
                variables = variables,
                relatedEntityType = relatedEntityType.ifBlank { eventType },
                relatedEntityId = relatedEntityId
            )
        }
    }

    suspend fun dispatchMessage(context: Context, message: CommunicationMessageEntity) {
        val adapter = adapters[message.channel.uppercase()]
        if (adapter == null) {
            deliveryTracking.recordStatusChange(
                message.id,
                DeliveryStatus.FAILED,
                "Unsupported channel: ${message.channel}"
            )
            return
        }

        deliveryTracking.recordStatusChange(
            message.id,
            DeliveryStatus.SENDING,
            "Handing off to adapter ${adapter.channelName}"
        )

        val result = adapter.send(context, message)

        if (result.isSuccess) {
            deliveryTracking.recordStatusChange(
                message.id,
                DeliveryStatus.DELIVERED,
                "Sent successfully ID: ${result.providerResponseId}"
            )
        } else {
            val nextRetry = message.retryCount + 1
            if (nextRetry <= message.maxRetries) {
                repository.saveMessage(
                    message.copy(
                        status = "RETRY",
                        retryCount = nextRetry,
                        deliveryStatusDetails = "Retry $nextRetry/${message.maxRetries}: ${result.errorMessage}"
                    )
                )
                deliveryTracking.recordStatusChange(
                    message.id,
                    DeliveryStatus.RETRY,
                    "Scheduled retry $nextRetry/${message.maxRetries}"
                )
            } else {
                deliveryTracking.recordStatusChange(
                    message.id,
                    DeliveryStatus.FAILED,
                    "Exceeded max retries: ${result.errorMessage}"
                )
            }
        }
    }

    suspend fun retryFailedMessage(context: Context, messageId: Long) {
        retryService.scheduleManualRetry(context, messageId) { ctx, msg ->
            dispatchMessage(ctx, msg)
        }
    }
}
