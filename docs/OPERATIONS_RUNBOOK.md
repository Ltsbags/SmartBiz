# SmartBiz Enterprise Operations Manual & Runbook

## 1. Operational Monitoring & Dashboards
The SRE & DevOps teams monitor system health via Prometheus & Grafana:
- **API Gateway Latency**: P99 Target <= 200ms
- **Database Connection Pool**: Utilization Target <= 75%
- **Redis Cache Hit Ratio**: Target >= 90%
- **Queue Worker Backlog**: Target <= 500 pending jobs

## 2. Standard Operational Procedures (SOP)

### SOP-01: Scale Up Pod Replicas
When CPU utilization exceeds 75% or during peak business hours:
```bash
kubectl scale deployment/smartbiz-api-deployment --replicas=10 -n production
```

### SOP-02: Flush L2 Redis Cache
In case of cache staleness or data synchronization anomalies:
```bash
kubectl exec -it deployment/smartbiz-api-deployment -n production -- redis-cli -h redis-service FLUSHDB
```

### SOP-03: Rollback Faulty Release
If error rate spikes above 2% immediately following a deployment:
```bash
kubectl rollout undo deployment/smartbiz-api-deployment -n production
```
