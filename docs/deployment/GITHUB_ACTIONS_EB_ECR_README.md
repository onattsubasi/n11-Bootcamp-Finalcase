# Finalcase GitHub Actions + Elastic Beanstalk Deployment Files

Copy these files into the repository root.

Generated for this repository structure:

```text
n11-Bootcamp-Finalcase/
├── backend/
│   ├── pom.xml
│   ├── common/
│   └── services/
├── frontend/
├── .github/workflows/
└── deploy/aws/elasticbeanstalk/
```

## Files

```text
.github/workflows/ci.yml
.github/workflows/deploy-elastic-beanstalk.yml
.github/workflows/deploy-frontend.yml
deploy/aws/elasticbeanstalk/docker-compose.aws-eb.template.yml
```

## Required GitHub Repository Secrets

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
DIGITALOCEAN_ACCESS_TOKEN
SLACK_WEBHOOK_URL
```

## Required GitHub Repository Variables

```text
AWS_REGION=eu-central-1
EB_APPLICATION_NAME=finalcase-backend
EB_ENVIRONMENT_NAME=Finalcase-backend-env
EB_DEPLOY_BUCKET=finalcase-eb-deploy-<unique-suffix>
ECR_REPOSITORY_PREFIX=finalcase
FRONTEND_APP_NAME=finalcase-frontend
VITE_API_BASE_URL=http://finalcase-backend.eu-central-1.elasticbeanstalk.com
```

Use a globally unique S3 bucket name for `EB_DEPLOY_BUCKET`, for example:

```text
finalcase-eb-deploy-096527555730-eu-central-1
```

## AWS IAM notes

The GitHub Actions IAM user needs permissions for:

```text
Elastic Beanstalk application versions/environment update
S3 upload to EB deployment bucket
ECR create repository, login, push images
```

The Elastic Beanstalk EC2 instance role needs permission to pull ECR images:

```text
AmazonEC2ContainerRegistryReadOnly
```

## Important

The workflow uses AWS ECR, not GHCR.

This avoids making GitHub container packages public and is cleaner for Elastic Beanstalk.
