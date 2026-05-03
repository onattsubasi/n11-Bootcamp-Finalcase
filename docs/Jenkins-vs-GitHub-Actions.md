# Jenkins vs GitHub Actions — Pipeline Comparison

The bootcamp asks for understanding the pipeline concept, including a Jenkins comparison.

## Shared pipeline idea

Both Jenkins and GitHub Actions implement the same basic CI/CD pipeline concept:

```text
source code change
  -> checkout
  -> build
  -> test
  -> package
  -> build image
  -> push image
  -> deploy
  -> notify
```

## GitHub Actions

GitHub Actions is integrated into GitHub.

Advantages:

```text
- no separate CI server required
- workflow files live inside the repository
- easy secret management through GitHub Secrets
- good for bootcamp and cloud-native projects
- supports matrix builds
- good marketplace actions for AWS, Docker, Slack, Node, Java
```

Tradeoffs:

```text
- less control over runner environment unless using self-hosted runners
- complex enterprise pipelines may need more structure
```

## Jenkins

Jenkins is a standalone CI/CD server.

Advantages:

```text
- very flexible
- mature plugin ecosystem
- can run fully inside private networks
- powerful for complex enterprise pipelines
- supports scripted and declarative pipelines
```

Tradeoffs:

```text
- needs server setup and maintenance
- plugin/version management can become operational work
- credential and agent management are additional responsibilities
```

## Mapping this project's pipeline

| Pipeline step | GitHub Actions | Jenkins |
|---|---|---|
| Checkout | actions/checkout | checkout scm |
| Java setup | actions/setup-java | tool / agent image |
| Maven build | mvn verify | sh 'mvn verify' |
| Jib image build | jib:build | sh 'mvn jib:build' |
| Push image | AWS ECR login action | ECR plugin or AWS CLI |
| Deploy | AWS CLI / EB action | AWS CLI / plugin |
| Slack notification | curl or Slack action | Slack plugin or curl |

## Final decision

Use GitHub Actions for this project because the bootcamp explicitly asks for it and it is simpler to demonstrate.
Jenkins is explained conceptually as an alternative pipeline runner.
