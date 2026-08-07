# SmartBiz Enterprise Backup & Disaster Recovery (DR) Plan

## 1. Recovery Objectives
- **Recovery Point Objective (RPO)**: <= 5 Minutes (Max allowable data loss)
- **Recovery Time Objective (RTO)**: <= 15 Minutes (Max allowable service downtime)

## 2. Automated Backup Strategy
- **PostgreSQL Database**:
  - Continuous WAL (Write-Ahead Logging) archiving to S3/MinIO
  - Daily full database snapshot at 01:00 UTC
  - Retained for 30 days in immutable object storage
- **SQLite Client App Local State**:
  - Incremental WAL checkpoints during idle states
  - Encrypted cloud backup sync via WAL stream

## 3. Disaster Recovery Execution Protocol

### Step 1: Restore Database Snapshot
```bash
# Execute Point-in-Time Recovery (PITR)
pg_restore -h postgres-service.production -U smartbiz_admin -d smartbiz_prod /backups/snapshot_latest.dump
```

### Step 2: Validate Data Integrity
Run automated SQL data integrity & hash validation scripts.

### Step 3: Switch Traffic via DNS / Ingress Routing
Reroute Ingress or Global Load Balancer to Disaster Recovery (DR) Secondary Region.
