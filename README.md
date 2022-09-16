# Spring Boot on AWS

Example project to deploy a Spring boot application to AWS Fargate using AWS CDK java.
The project is based in https://github.com/enghwa/springboot-fargate-cdk

## Build the project

`./gradlew build`

## How to run it?

Run the DB:
```
% docker run -e POSTGRES_PASSWORD=example -e POSTGRES_USER=example -e POSTGRES_DB=example -p 5432:5432 postgres:14-alpine
```

Run the application:
```
% cd application/
% ../gradlew bootRun
```

## How to deploy it?

This project uses GitHub Actions to deploy the infrastructure, so you only have to provide the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
