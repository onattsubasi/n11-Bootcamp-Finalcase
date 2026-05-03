# Jib Usage

Jib builds OCI/Docker images for Java applications without a Dockerfile.

## Build local Docker image

```bash
cd backend
mvn -pl services/auth-service -am compile com.google.cloud.tools:jib-maven-plugin:3.4.5:dockerBuild \
  -Dimage=finalcase/auth-service:local
```

## Push image to registry

```bash
cd backend
mvn -pl services/auth-service -am compile com.google.cloud.tools:jib-maven-plugin:3.4.5:build \
  -Dimage=ghcr.io/your-user/finalcase-auth-service:latest
```

## Why Jib here?

```text
- satisfies bootcamp requirement: Dockerfile-free image creation
- works directly from Maven
- reproducible Java container image layers
- no Dockerfile duplication per service
```
