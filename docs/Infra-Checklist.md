# Infrastructure Checklist

## Bootcamp-required items

- [ ] Backend services can be containerized
- [ ] Jib builds images without Dockerfile
- [ ] GitHub Actions CI runs Maven build
- [ ] GitHub Actions can deploy
- [ ] Jenkins comparison documented
- [ ] AWS Elastic Beanstalk deployment path exists
- [ ] AWS RDS PostgreSQL plan exists
- [ ] Slack deploy notification exists

## Recommended observability items

- [ ] Services expose `/actuator/health`
- [ ] Services expose `/actuator/prometheus`
- [ ] Prometheus scrapes services
- [ ] Grafana datasource provisioning works
- [ ] Loki receives logs through Promtail
- [ ] Jaeger receives traces through OpenTelemetry Collector
- [ ] Gateway creates/forwards `X-Correlation-Id`
- [ ] Logs include correlationId
- [ ] Internal endpoints are not exposed through Gateway
