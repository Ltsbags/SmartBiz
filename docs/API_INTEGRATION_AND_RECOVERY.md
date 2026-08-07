# SmartBiz Enterprise Cloud - API Integration & Disaster Recovery Guide

## 1. REST API & Webhook Specifications

### 1.1 Payment Processing Endpoints
- `POST /api/v1/payments/process` - Submits a payment request to the designated gateway adapter.
- `POST /api/v1/payments/link` - Generates a dynamic payment link and UPI QR payload.
- `POST /api/v1/payments/refund` - Issues a full or partial refund with accounting ledger reversal.
- `POST /api/v1/payments/reconcile` - Reconciles gateway settlement statements against local records.

### 1.2 Webhook Integration Format
Gateways (Razorpay, Stripe, Bank APIs) send webhooks to:
- `POST /api/v1/webhooks/payments/:provider`
Headers must include HMAC signatures (`X-Razorpay-Signature`, `Stripe-Signature`).

---

## 2. Synchronization & Realtime Protocol

### 2.1 Sync Protocol
1. Client sends local mutations from `offline_sync_queue` with `sequence_id` and timestamp.
2. Server validates entity state and resolves conflicts using **Last-Write-Wins (LWW)** or **Vector Clocks**.
3. Server returns delta payload; client updates local Room database atomically.

### 2.2 WebSocket Gateway Protocol
- Channel `/ws/realtime`: Receives live presence, stock adjustments, and sales invoice notifications.

---

## 3. Disaster Recovery & Security Audit Specifications
- Backup Strategy: Automated daily snapshots stored in encrypted GCS bucket.
- Data Encryption: AES-256 for data at rest (Room DB / SQL), TLS 1.3 for data in transit.
- Audit Logging: Immutable audit logs with timestamp, user ID, module, action, and severity.
