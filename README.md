# Spring Boot on AWS

Example project to deploy a Spring boot application to AWS Fargate using AWS CDK java.
The project is based in https://github.com/enghwa/springboot-fargate-cdk

## Build the project

`./gradlew build`

## How to run it?

Set the environment (DB):
```
% docker compose up
```

Run the application:
```
% ./gradlew bootRun
```

## How to deploy it?

This project uses GitHub Actions to deploy the infrastructure, so you only have to provide the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
