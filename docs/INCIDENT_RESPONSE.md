# SmartBiz Enterprise Incident Response Guide

## 1. Severity Matrix
| Severity | Impact | Trigger Condition | Response SLA |
| :--- | :--- | :--- | :--- |
| **SEV-1 (Critical)** | System Down / Data Corruption | Outage > 2 mins, Error Rate > 5% | 15 Minutes |
| **SEV-2 (High)** | Degradation / Secondary Feature Outage | Sync Delay > 10 mins, High CPU | 30 Minutes |
| **SEV-3 (Medium)** | Minor Bug / Non-critical UI anomaly | Single user impact | 4 Hours |

## 2. Automated Alerting & Escalation
Prometheus Alertmanager triggers PagerDuty escalation policies:
- High CPU / Memory (>85% for 5 mins)
- DB Connection Timeout
- Dead-Letter Queue (DLQ) threshold breach (>10 dead jobs)

## 3. Incident Triage Steps
1. **Declare Incident**: Open PagerDuty Incident & War Room Channel.
2. **Isolate Component**: Enable Circuit Breaker to isolate degraded sub-service.
3. **Capture Logs**:
   ```bash
   kubectl logs -l app=smartbiz-api --tail=500 -n production > incident_logs.json
   ```
4. **Remediate**: Apply hotfix or trigger zero-downtime rollback.
5. **Post-Mortem**: Conduct Root Cause Analysis (RCA) within 48 hours.
