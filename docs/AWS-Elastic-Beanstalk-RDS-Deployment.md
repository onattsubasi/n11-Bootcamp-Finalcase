# AWS Elastic Beanstalk + RDS Deployment Direction

This project targets a bootcamp-friendly production-like deployment:

```text
GitHub Actions
  -> Maven verify
  -> Jib image build
  -> AWS ECR image push
  -> Elastic Beanstalk Docker Compose deployment
  -> AWS RDS PostgreSQL
  -> Slack deploy notification
```

## AWS resources to create

```text
- AWS RDS PostgreSQL instance
- Elastic Beanstalk application
- Elastic Beanstalk environment using Docker platform
- AWS ECR repositories for service images
- S3 bucket for Elastic Beanstalk deployment bundles
- IAM user/role for GitHub Actions deployment
```

## Required GitHub Secrets

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
ECR_REPOSITORY_PREFIX
EB_APPLICATION_NAME
EB_ENVIRONMENT_NAME
EB_DEPLOY_BUCKET
SLACK_WEBHOOK_URL
```

## Required Elastic Beanstalk environment variables

```text
RDS_HOSTNAME
RDS_PORT
RDS_USERNAME
RDS_PASSWORD
JWT_SECRET
JWT_ISSUER
REFRESH_TOKEN_PEPPER
RABBITMQ_DEFAULT_USER
RABBITMQ_DEFAULT_PASS
IYZICO_API_KEY
IYZICO_SECRET_KEY
IYZICO_BASE_URL
PAYMENT_PUBLIC_BASE_URL
```

## Database preparation

Create these databases on the RDS PostgreSQL instance:

```text
auth_db
user_db
catalog_db
search_db
basket_db
inventory_db
promotion_db
checkout_db
order_db
payment_db
shipment_db
notification_db
review_db
```

Each service runs its own Flyway migrations against its own database.

## Important limitation

Running all microservices in one Elastic Beanstalk Docker Compose environment is acceptable for bootcamp demonstration, but a real production deployment would usually move this to ECS/Fargate, EKS, or separate service deployments.
