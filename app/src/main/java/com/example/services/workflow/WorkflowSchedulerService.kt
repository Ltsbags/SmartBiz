package com.example.services.workflow

import com.example.services.workflow.models.DomainEvent
import java.util.UUID

data class ScheduledJob(
    val id: String = UUID.randomUUID().toString(),
    val workflowId: String,
    val scheduleType: String, // ONE_TIME, DAILY, WEEKLY, MONTHLY, CRON
    val cronExpression: String? = null,
    val nextRunTime: Long,
    val isActive: Boolean = true
)

class WorkflowSchedulerService(
    private val workflowEngine: WorkflowEngine
) {
    private val activeJobs = mutableListOf<ScheduledJob>()

    fun scheduleWorkflow(workflowId: String, scheduleType: String, cronExpression: String? = null, delayMs: Long = 0): ScheduledJob {
        val nextRun = System.currentTimeMillis() + delayMs
        val job = ScheduledJob(
            workflowId = workflowId,
            scheduleType = scheduleType,
            cronExpression = cronExpression,
            nextRunTime = nextRun
        )
        activeJobs.add(job)
        return job
    }

    suspend fun triggerScheduledRun(job: ScheduledJob) {
        val event = DomainEvent(
            eventType = "SCHEDULE_TRIGGER",
            entityId = job.workflowId,
            payload = mapOf("scheduleType" to job.scheduleType, "jobId" to job.id)
        )
        workflowEngine.onDomainEvent(event)
    }

    fun getActiveJobs(): List<ScheduledJob> = activeJobs.toList()
}
