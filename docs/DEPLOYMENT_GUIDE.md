# SmartBiz Enterprise Deployment Guide

## 1. Architecture Overview
SmartBiz Enterprise utilizes a cloud-native microservices architecture orchestrated via Kubernetes with automated GitHub Actions CI/CD pipelines.

```
[ Developer Commit ] -> [ GitHub Actions ] -> [ Docker Registry ] -> [ K8s Rolling Deploy ]
                                                                             |
                                                                   [ Production Cluster ]
```

## 2. Environments Configuration
Independent environment configurations are maintained via Kubernetes ConfigMaps and Helm values:

- **Development**: Minikube / Local K3s, Mocked Gateway
- **Staging**: `staging` Namespace, Sandbox API Gateways
- **Production**: `production` Namespace, High-Availability Multi-Zone Cluster

## 3. Zero-Downtime Deployment Strategy
We deploy using **Rolling Updates** backed by Kubernetes Readiness/Liveness Probes and PodDisruptionBudgets:
- `maxSurge: 1` (Spins up 1 new container prior to draining old ones)
- `maxUnavailable: 0` (Guarantees zero dropped HTTP requests during release)

```bash
# Execute zero-downtime deployment
kubectl apply -f k8s/configmap-secrets.yaml
kubectl apply -f k8s/deployment.yaml
kubectl rollout status deployment/smartbiz-api-deployment -n production
```

## 4. Environment Variables & External Secrets
Secrets are externalized via KMS / HashiCorp Vault into Kubernetes Secrets:
- `DATABASE_PASSWORD`: AES-256 encrypted
- `JWT_SECRET_KEY`: Rotated bi-monthly
- `GEMINI_API_KEY`: Server-side secure token
